package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/**
 * Changes an arg of the story on show, e.g.: `remaining` to `3`, and keeps it in the location, so a link opens the story
 * with it. Nothing happens when the arg has that value. Fails when its control does not accept it.
 */
class ChangeArg(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        name: String,
        value: ArgDto,
    ) {
        val storybook = storybookRepository.search() ?: return
        val (events, changedStorybook) = storybook.changeArg(name, value).pullEvents()
        if (events.isEmpty()) return
        storybookRepository.save(changedStorybook)
        storyLocation.replace(changedStorybook.toLocation())
        eventBus.publish(events)
    }
}
