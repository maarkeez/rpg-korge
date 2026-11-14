package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.storybook.domain.StorybookRepository
import com.mkz.tessavar.storybook.usecases.services.StoryLocation

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
