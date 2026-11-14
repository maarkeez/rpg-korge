package com.mkz.tessavar.storybook.adapters.location

import com.mkz.tessavar.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.tessavar.storybook.usecases.services.StoryLocation

class NoStoryLocation : StoryLocation {
    override fun search() = LocationDto(storyId = null, args = emptyMap(), globals = emptyMap())

    override fun replace(location: LocationDto) {
    }
}
