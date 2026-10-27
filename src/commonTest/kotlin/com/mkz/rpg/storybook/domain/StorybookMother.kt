package com.mkz.rpg.storybook.domain

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto

object StorybookMother {
    fun storybook(
        components: List<ComponentDto> = listOf(component()),
        requestedStoryId: String? = null,
    ): Storybook = Storybook.create(components, requestedStoryId).pullEvents().second

    fun component(
        id: String = "screen-health-bar",
        group: String = "Screen",
        title: String = "Health bar",
        description: String = "The health of a battle unit.",
        stories: List<StoryDto> = listOf(story(id = "$id--full")),
    ): ComponentDto = ComponentDto(id = id, group = group, title = title, description = description, stories = stories)

    fun story(
        id: String = "screen-health-bar--full",
        name: String = "Full",
        description: String = "Every health point left.",
    ): StoryDto = StoryDto(id = id, name = name, description = description)
}
