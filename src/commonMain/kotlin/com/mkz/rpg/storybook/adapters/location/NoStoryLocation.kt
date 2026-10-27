package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** Keeps no story, where there is no URL to keep it in (e.g.: on the JVM): the Storybook opens on its first story. */
class NoStoryLocation : StoryLocation {
    override fun searchStoryId(): String? = null

    override fun replaceStoryId(storyId: String) {
        // There is nowhere to keep it.
    }
}
