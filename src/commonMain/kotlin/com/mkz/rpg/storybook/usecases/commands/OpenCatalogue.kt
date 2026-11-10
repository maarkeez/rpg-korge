package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.Storybook
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.services.StoryLocation

class OpenCatalogue(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        components: List<ComponentDto>,
        globalControls: List<ControlDto>,
        defaultGlobals: Map<String, ValueDto>,
    ) {
        val (events, storybook) = Storybook.create(components, globalControls, defaultGlobals, location = storyLocation.search()).pullEvents()
        storybookRepository.save(storybook)
        eventBus.publish(events)
    }
}
