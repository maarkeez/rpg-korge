package com.mkz.tessavar.storybook.usecases.services

import com.mkz.tessavar.storybook.domain.Storybook.Dto.LocationDto

class FakeStoryLocation(
    private val requestedStoryId: String? = null,
    private val requestedArgs: Map<String, String> = emptyMap(),
    private val requestedGlobals: Map<String, String> = emptyMap(),
) : StoryLocation {
    val keptLocations = mutableListOf<LocationDto>()
    val keptStoryIds get() = keptLocations.map { location -> location.storyId }

    override fun search() = LocationDto(storyId = requestedStoryId, args = requestedArgs, globals = requestedGlobals)

    override fun replace(location: LocationDto) {
        keptLocations += location
    }
}
