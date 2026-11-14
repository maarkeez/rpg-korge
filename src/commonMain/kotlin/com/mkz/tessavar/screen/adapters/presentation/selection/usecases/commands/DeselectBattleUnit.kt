package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.tessavar.shared.domain.EventBus

class DeselectBattleUnit(
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        val (events, updatedSelection) =
            when (selection) {
                is NothingSelected -> selection.deselect().pullEvents()
                is BattleUnitSelected -> selection.deselect().pullEvents()
                is AbilitySelected -> selection.deselect().pullEvents()
                is AbilityInfoDisplayed -> selection.deselect().pullEvents()
                is CastPreviewed -> selection.deselect().pullEvents()
            }
        selectionRepository.update(updatedSelection)
        eventBus.publish(events)
    }
}
