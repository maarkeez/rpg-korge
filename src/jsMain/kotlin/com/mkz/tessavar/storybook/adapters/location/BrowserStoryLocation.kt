package com.mkz.tessavar.storybook.adapters.location

import com.mkz.tessavar.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.tessavar.storybook.usecases.services.StoryLocation
import kotlinx.browser.window

class BrowserStoryLocation : StoryLocation {
    override fun search(): LocationDto = LocationQuery.decode(window.location.search)

    override fun replace(location: LocationDto) {
        val query = LocationQuery.encode(location, window.location.search)
        window.history.replaceState(null, "", "${window.location.pathname}${if (query.isEmpty()) "" else "?$query"}${window.location.hash}")
    }
}
