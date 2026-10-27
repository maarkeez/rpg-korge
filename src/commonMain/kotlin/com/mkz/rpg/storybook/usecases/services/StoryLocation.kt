package com.mkz.rpg.storybook.usecases.services

/** Where the story on show is kept, so a link opens it, e.g.: the page URL `?story=screen-health-bar--full`. */
interface StoryLocation {
    /** The id of the story asked for, or `null` when none is. */
    fun searchStoryId(): String?

    /** Keeps the story on show in place of the one kept, so going back does not step through every story shown. */
    fun replaceStoryId(storyId: String)
}
