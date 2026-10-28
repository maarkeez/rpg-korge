package com.mkz.rpg.storybook.adapters.page

// Records the stories rendered, instead of titling a browser page.
class FakeStorybookPage : StorybookPage {
    val renderedStoryIds = mutableListOf<String>()

    override fun storyRendered(storyId: String) {
        renderedStoryIds += storyId
    }
}
