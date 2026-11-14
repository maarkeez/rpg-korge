package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ActionDto

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
