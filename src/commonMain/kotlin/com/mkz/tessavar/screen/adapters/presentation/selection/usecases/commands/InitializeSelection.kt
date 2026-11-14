package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionRepository

class InitializeSelection(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke() {
        selectionRepository.create(NothingSelected.create())
    }
}
