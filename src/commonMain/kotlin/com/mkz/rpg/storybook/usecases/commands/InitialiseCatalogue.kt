package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.Storybook
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/**
 * Opens the catalogue of the components on the story the location asks for, e.g.: the story of a link, with the args
 * and globals it asks for. Anything unknown or invalid is ignored, never a failure, as a link may outlive its story: the
 * first story is opened instead, and an invalid arg or global keeps its own value.
 */
class InitialiseCatalogue(
    private val storybookRepository: StorybookRepository,
    private val storyLocation: StoryLocation,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        components: List<ComponentDto>,
        globalControls: List<ControlDto>,
        defaultGlobals: Map<String, ArgDto>,
    ) {
        val (events, storybook) = Storybook.create(components, globalControls, defaultGlobals, location = storyLocation.search()).pullEvents()
        storybookRepository.save(storybook)
        eventBus.publish(events)
    }
}
