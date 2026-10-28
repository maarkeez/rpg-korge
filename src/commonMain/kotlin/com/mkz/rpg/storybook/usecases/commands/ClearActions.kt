package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.StorybookRepository

/** Empties the log of actions. Nothing happens when it is empty. */
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
