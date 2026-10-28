package com.mkz.rpg.storybook.adapters.page

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class StorybookPageTest {
    @Test
    fun `renderedTitle should be the Storybook and the story id, as a script waits for it`() {
        // When
        val title = StorybookPage.renderedTitle("screen-shop--buy")
        // Then
        title shouldBe "Storybook – screen-shop--buy"
        title shouldStartWith StorybookPage.RENDERED_TITLE_PREFIX
    }
}
