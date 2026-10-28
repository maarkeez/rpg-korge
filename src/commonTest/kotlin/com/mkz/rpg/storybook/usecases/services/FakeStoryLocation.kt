package com.mkz.rpg.storybook.usecases.services

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto

// Asks for the given story, args and globals, and records what is kept, instead of reading and writing a page URL.
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
