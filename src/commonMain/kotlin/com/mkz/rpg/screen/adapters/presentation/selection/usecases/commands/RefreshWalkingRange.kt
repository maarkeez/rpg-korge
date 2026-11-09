package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.rpg.shared.domain.EventBus

/**
 * Displays again the selected battle unit with its current tile and walking range, e.g.: after it moves or casts,
 * or after a tile is occupied or an occupant removed, since that may open or close paths.
 *
 * Out of the battles (e.g.: while exploring), the selected battle unit has no walking range, so it is only displayed
 * again when its tile changes. Otherwise, the CPU battle units wandering every second would display its info again in
 * place of its inventory.
 */
class RefreshWalkingRange(
    private val searchPosition: SearchPosition,
    private val searchBattle: SearchBattle,
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
    private val walkingRangeService: WalkingRangeService,
) {
    operator fun invoke() {
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        when (selection) {
            is BattleUnitSelected -> {
                val battleUnitId = selection.battleUnitId
                val position = searchPosition(battleUnitId)
                if (position == null) {
                    // The selected battle unit is no longer on the battlefield, e.g.: it has been defeated.
                    val (events, updatedSelection) = selection.deselect().pullEvents()
                    selectionRepository.update(updatedSelection)
                    eventBus.publish(events)
                    return
                }
                val tile = TileDto(position.row, position.column)
                val isBattleInProgress = searchBattle()?.isFinished == false
                if (!isBattleInProgress && tile == selection.tile) return
                val walkingRange = if (isBattleInProgress) walkingRangeService.walkingRange(battleUnitId) else emptySet()
                val (events, updatedSelection) =
                    selection
                        .refreshWalkingRange(tile = tile, walkingRange = walkingRange)
                        .pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            }
            is NothingSelected,
            is AbilitySelected,
            is AbilityInfoDisplayed,
            is CastPreviewed,
            -> return
        }
    }
}
