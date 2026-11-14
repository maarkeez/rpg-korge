package com.mkz.tessavar.screen.adapters.presentation.selection.domain

interface SelectionRepository {
    fun create(selection: Selection)

    fun update(selection: Selection)

    fun search(): Selection?
}
