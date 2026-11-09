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
 * Selects the battle unit with its walking range, as if the player had selected its tile, replacing any selection,
 * e.g.: the party leader when a human turn starts. A battle unit off the battlefield is not selected: the selection deselects.
 *
 * Out of the battles (e.g.: while exploring, the party member the inventory switches to), it has no walking range.
 */
class SelectBattleUnit(
    private val searchPosition: SearchPosition,
    private val searchBattle: SearchBattle,
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
    private val walkingRangeService: WalkingRangeService,
) {
    operator fun invoke(battleUnitId: String) {
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        val nothingSelected =
            when (selection) {
                is NothingSelected -> selection
                is BattleUnitSelected -> selection.deselect()
                is AbilitySelected -> selection.deselect()
                is AbilityInfoDisplayed -> selection.deselect()
                is CastPreviewed -> selection.deselect()
            }
        val position = searchPosition(battleUnitId)
        val isBattleInProgress = searchBattle()?.isFinished == false
        val (events, updatedSelection) =
            if (position == null) {
                nothingSelected.deselect().pullEvents()
            } else {
                nothingSelected
                    .selectBattleUnit(
                        tile = TileDto(row = position.row, column = position.column),
                        battleUnitId = battleUnitId,
                        walkingRange = if (isBattleInProgress) walkingRangeService.walkingRange(battleUnitId) else emptySet(),
                    ).pullEvents()
            }
        selectionRepository.update(updatedSelection)
        eventBus.publish(events)
    }
}
