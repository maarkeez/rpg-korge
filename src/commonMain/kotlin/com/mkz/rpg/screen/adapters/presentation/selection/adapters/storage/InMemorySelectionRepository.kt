package com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage

import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionRepository

class InMemorySelectionRepository : SelectionRepository {
    private var selection: Selection? = null

    override fun create(selection: Selection) {
        this.selection = selection
    }

    override fun update(selection: Selection) {
        this.selection = selection
    }

    override fun search(): Selection? = selection
}
