package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.storybook.domain.StorybookRepository
import com.mkz.tessavar.storybook.usecases.services.StoryLocation

class SelectStory(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke(storyId: String) {
        val storybook = storybookRepository.search() ?: return
        val (events, selectedStorybook) = storybook.selectStory(storyId).pullEvents()
        if (events.isEmpty()) return
        storybookRepository.save(selectedStorybook)
        storyLocation.replace(selectedStorybook.toLocation())
        eventBus.publish(events)
    }
}
