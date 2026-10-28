package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** Keeps nothing, where there is no URL to keep it in (e.g.: on the JVM): the Storybook opens on its first story. */
class NoStoryLocation : StoryLocation {
    override fun search() = LocationDto(storyId = null, args = emptyMap(), globals = emptyMap())

    override fun replace(location: LocationDto) {
        // There is nowhere to keep it.
    }
}
