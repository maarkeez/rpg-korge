package com.mkz.rpg.storybook.adapters.page

interface StorybookPage {
    companion object {
        const val RENDERED_TITLE_PREFIX = "Storybook – "

        fun renderedTitle(storyId: String) = "$RENDERED_TITLE_PREFIX$storyId"
    }

    fun storyRendered(storyId: String)
}
