package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
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
                assertThat(cancelButton.buttonText()).isEqualTo("Cancel")
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
                assertThat(cancelled).isTrue
            }
    }
}
