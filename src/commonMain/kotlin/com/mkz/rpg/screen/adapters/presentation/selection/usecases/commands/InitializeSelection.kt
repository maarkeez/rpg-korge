package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionRepository

class InitializeSelection(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke() {
        selectionRepository.create(NothingSelected.create())
    }
}
