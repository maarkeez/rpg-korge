package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.tessavar.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.tessavar.shared.domain.EventBus

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
