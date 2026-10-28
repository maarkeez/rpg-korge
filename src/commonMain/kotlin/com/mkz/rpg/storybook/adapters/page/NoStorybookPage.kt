package com.mkz.rpg.storybook.adapters.page

/** Tells no one, where there is no page (e.g.: on the JVM). */
class NoStorybookPage : StorybookPage {
    override fun storyRendered(storyId: String) {
        // There is no page to tell.
    }
}
