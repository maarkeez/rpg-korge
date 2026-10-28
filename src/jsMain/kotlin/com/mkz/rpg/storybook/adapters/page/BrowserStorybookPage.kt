package com.mkz.rpg.storybook.adapters.page

import kotlinx.browser.document

/**
 * Titles the browser page after the story on show once it has rendered, e.g.: `Storybook – screen-shop--buy`, which a
 * script waits for before taking a screenshot (see [StorybookPage.renderedTitle]).
 */
class BrowserStorybookPage : StorybookPage {
    override fun storyRendered(storyId: String) {
        document.title = StorybookPage.renderedTitle(storyId)
    }
}
