package com.mkz.rpg.storybook.domain

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto

object StorybookMother {
    val LANGUAGE_CONTROL = ControlDto.pick("language", listOf("en", "es"))

    fun storybook(
        components: List<ComponentDto> = listOf(component()),
        requestedStoryId: String? = null,
        requestedArgs: Map<String, String> = emptyMap(),
        requestedGlobals: Map<String, String> = emptyMap(),
        globalControls: List<ControlDto> = listOf(LANGUAGE_CONTROL),
        defaultGlobals: Map<String, ArgDto> = mapOf("language" to ArgDto.text("en")),
    ): Storybook = Storybook.create(components, globalControls, defaultGlobals, location(requestedStoryId, requestedArgs, requestedGlobals)).pullEvents().second

    fun component(
        id: String = "screen-health-bar",
        group: String = "Screen",
        title: String = "Health bar",
        description: String = "The health of a battle unit.",
        controls: List<ControlDto> = listOf(ControlDto.int("remaining", minimum = 0, maximum = 30)),
        args: Map<String, ArgDto> = mapOf("remaining" to ArgDto.int(30)),
        stories: List<StoryDto> = listOf(story(id = "$id--full")),
    ): ComponentDto = ComponentDto(id = id, group = group, title = title, description = description, controls = controls, args = args, stories = stories)

    fun story(
        id: String = "screen-health-bar--full",
        name: String = "Full",
        description: String = "Every health point left.",
        args: Map<String, ArgDto> = emptyMap(),
    ): StoryDto = StoryDto(id = id, name = name, description = description, args = args)

    fun location(
        storyId: String? = null,
        args: Map<String, String> = emptyMap(),
        globals: Map<String, String> = emptyMap(),
    ): LocationDto = LocationDto(storyId = storyId, args = args, globals = globals)
}
