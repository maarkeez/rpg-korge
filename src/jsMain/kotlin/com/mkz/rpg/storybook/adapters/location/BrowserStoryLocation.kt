package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

/**
 * Keeps the story on show in the URL of the page, e.g.: `/storybook/?story=screen-health-bar--full`, so the URL is a
 * link to it, which also opens it after a reload. The URL is replaced rather than pushed, so going back leaves the
 * Storybook instead of stepping through every story shown.
 */
class BrowserStoryLocation : StoryLocation {
    companion object {
        const val STORY_PARAMETER = "story"
    }

    override fun searchStoryId(): String? = URLSearchParams(window.location.search).get(STORY_PARAMETER)

    override fun replaceStoryId(storyId: String) {
        val parameters = URLSearchParams(window.location.search)
        parameters.set(STORY_PARAMETER, storyId)
        // From the path of the page, so the other parameters and the hash are kept.
        window.history.replaceState(null, "", "${window.location.pathname}?$parameters${window.location.hash}")
    }
}
