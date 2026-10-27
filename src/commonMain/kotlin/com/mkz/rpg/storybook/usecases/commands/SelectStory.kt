package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** Shows the story, and keeps it in the location, so a link opens it. Nothing happens when it is already on show. */
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
        storyLocation.replaceStoryId(storyId)
        eventBus.publish(events)
    }
}
