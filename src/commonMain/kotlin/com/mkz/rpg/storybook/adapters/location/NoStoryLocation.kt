package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.rpg.storybook.usecases.services.StoryLocation

class NoStoryLocation : StoryLocation {
    override fun search() = LocationDto(storyId = null, args = emptyMap(), globals = emptyMap())

    override fun replace(location: LocationDto) {
    }
}
