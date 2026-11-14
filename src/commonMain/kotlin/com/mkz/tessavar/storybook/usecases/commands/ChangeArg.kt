package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.tessavar.storybook.domain.StorybookRepository
import com.mkz.tessavar.storybook.usecases.services.StoryLocation

class ChangeArg(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        name: String,
        value: ValueDto,
    ) {
        val storybook = storybookRepository.search() ?: return
        val (events, changedStorybook) = storybook.changeArg(name, value).pullEvents()
        if (events.isEmpty()) return
        storybookRepository.save(changedStorybook)
        storyLocation.replace(changedStorybook.toLocation())
        eventBus.publish(events)
    }
}
