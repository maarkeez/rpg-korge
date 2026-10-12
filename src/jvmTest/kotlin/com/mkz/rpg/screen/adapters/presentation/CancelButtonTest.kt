package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CancelButtonTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should display cancel text when is initialized`() =
            viewsTest {
                // Given
                // When
                val cancelButton = CancelButton(onCancelled = {})
                // Then
                cancelButton.buttonText() shouldBe "Cancel"
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should cancel when button is clicked`() =
            viewsTest {
                // Given
                var cancelled = false
                val cancelButton = CancelButton(onCancelled = { cancelled = true })
                addChild(cancelButton)
                // When
                clickCancel(cancelButton)
                // Then
                cancelled.shouldBeTrue()
            }
    }
}
