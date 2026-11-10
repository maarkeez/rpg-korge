package com.mkz.rpg.storybook.adapters.page

class FakeStorybookPage : StorybookPage {
    val renderedStoryIds = mutableListOf<String>()

    override fun storyRendered(storyId: String) {
        renderedStoryIds += storyId
    }
}
