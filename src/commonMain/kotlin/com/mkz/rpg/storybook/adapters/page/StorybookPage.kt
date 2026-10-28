package com.mkz.rpg.storybook.adapters.page

/**
 * The page the Storybook runs in, told when the story on show has rendered, so a script driving the browser (e.g.:
 * Playwright, see the `storybook` skill) knows when to take a screenshot, instead of waiting for the resources to load.
 */
interface StorybookPage {
    companion object {
        /** What the title of the page starts with once a story has rendered, followed by its id. */
        const val RENDERED_TITLE_PREFIX = "Storybook – "

        /** The title of the page once the story has rendered, e.g.: `Storybook – screen-shop--buy`. */
        fun renderedTitle(storyId: String) = "$RENDERED_TITLE_PREFIX$storyId"
    }

    /** The story, with its args and globals, has rendered in the phone, e.g.: `screen-shop--buy`. */
    fun storyRendered(storyId: String)
}
