package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.tessavar.shared.domain.EventBus

class CancelCast(
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        when (selection) {
            is CastPreviewed -> {
                val (events, updatedSelection) = selection.cancelCast().pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            }

            is AbilitySelected,
            is AbilityInfoDisplayed,
            is BattleUnitSelected,
            is NothingSelected,
            -> throw SelectionError.InvalidSelectionState()
        }
    }
}
