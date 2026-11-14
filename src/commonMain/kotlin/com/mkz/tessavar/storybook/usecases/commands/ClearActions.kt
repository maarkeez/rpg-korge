package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.storybook.domain.StorybookRepository

class ClearActions(
    private val storybookRepository: StorybookRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storybook = storybookRepository.search() ?: return
        val (events, clearedStorybook) = storybook.clearActions().pullEvents()
        if (events.isEmpty()) return
        storybookRepository.save(clearedStorybook)
        eventBus.publish(events)
    }
}
