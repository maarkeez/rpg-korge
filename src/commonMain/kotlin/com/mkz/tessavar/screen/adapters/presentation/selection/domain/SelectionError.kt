package com.mkz.tessavar.screen.adapters.presentation.selection.domain

sealed class SelectionError(
    message: String,
) : Throwable(message = message) {
    class SelectionNotFound : SelectionError("Selection not found")

    class InvalidSelectionState : SelectionError("Invalid selection state")
}
