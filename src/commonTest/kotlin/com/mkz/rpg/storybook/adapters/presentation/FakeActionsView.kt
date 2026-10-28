package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto

// Records the actions the presenter asks to display, instead of drawing them.
class FakeActionsView : ActionsView() {
    val displayedActions = mutableListOf<List<ActionDto>>()
    var delegate: Delegate? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(actions: List<ActionDto>) {
        displayedActions += actions
    }
}
