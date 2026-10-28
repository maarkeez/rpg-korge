package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** Gives the story on show its own args back, also in the location. Nothing happens when none was changed. */
class ResetArgs(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val storybook = storybookRepository.search() ?: return
        val (events, resetStorybook) = storybook.resetArgs().pullEvents()
        if (events.isEmpty()) return
        storybookRepository.save(resetStorybook)
        storyLocation.replace(resetStorybook.toLocation())
        eventBus.publish(events)
    }
}
