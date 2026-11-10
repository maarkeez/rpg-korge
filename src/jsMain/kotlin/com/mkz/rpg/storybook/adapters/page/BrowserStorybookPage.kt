package com.mkz.rpg.storybook.adapters.page

import kotlinx.browser.document

class BrowserStorybookPage : StorybookPage {
    override fun storyRendered(storyId: String) {
        document.title = StorybookPage.renderedTitle(storyId)
    }
}
