package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConfirmButtonTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should display confirm text when is initialized`() =
            viewsTest {
                // Given
                // When
                val confirmButton = ConfirmButton(onConfirmed = {})
                // Then
                assertThat(confirmButton.buttonText()).isEqualTo("Confirm")
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should confirm when button is clicked`() =
            viewsTest {
                // Given
                var confirmed = false
                val confirmButton = ConfirmButton(onConfirmed = { confirmed = true })
                addChild(confirmButton)
                // When
                clickConfirm(confirmButton)
                // Then
                assertThat(confirmed).isTrue
            }
    }
}
