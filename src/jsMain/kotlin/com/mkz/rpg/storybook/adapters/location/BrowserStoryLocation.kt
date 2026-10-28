package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.rpg.storybook.usecases.services.StoryLocation
import kotlinx.browser.window

/**
 * Keeps what is on show in the URL of the page, e.g.: `/storybook/?story=screen-health-bar--full&args=remaining:3`, so
 * the URL is a link to it, which also opens it after a reload. See [LocationQuery]. The URL is replaced rather than
 * pushed, so going back leaves the Storybook instead of stepping through every change.
 */
class BrowserStoryLocation : StoryLocation {
    override fun search(): LocationDto = LocationQuery.decode(window.location.search)

    override fun replace(location: LocationDto) {
        val query = LocationQuery.encode(location, window.location.search)
        // From the path of the page, so the hash is kept.
        window.history.replaceState(null, "", "${window.location.pathname}${if (query.isEmpty()) "" else "?$query"}${window.location.hash}")
    }
}
