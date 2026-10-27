package com.mkz.rpg.storybook.usecases.services

// Asks for the given story, and records the stories kept, instead of reading and writing a page URL.
class FakeStoryLocation(
    private val requestedStoryId: String? = null,
) : StoryLocation {
    val keptStoryIds = mutableListOf<String>()

    override fun searchStoryId(): String? = requestedStoryId

    override fun replaceStoryId(storyId: String) {
        keptStoryIds += storyId
    }
}
