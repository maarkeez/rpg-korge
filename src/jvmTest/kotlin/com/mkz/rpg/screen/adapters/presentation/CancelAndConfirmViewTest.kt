package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CancelAndConfirmViewTest : ViewsForTesting() {
    @Nested
    inner class CancelClick {
        @Test
        fun `should only cancel when cancel button is clicked`() =
            viewsTest {
                // Given
                var cancelled = false
                var confirmed = false
                val cancelAndConfirmView = CancelAndConfirmView(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
                addChild(cancelAndConfirmView)
                // When
                clickCancel(cancelAndConfirmView)
                // Then
                assertThat(cancelled).isTrue
                assertThat(confirmed).isFalse
            }
    }

    @Nested
    inner class ConfirmClick {
        @Test
        fun `should only confirm when confirm button is clicked`() =
            viewsTest {
                // Given
                var cancelled = false
                var confirmed = false
                val cancelAndConfirmView = CancelAndConfirmView(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
                addChild(cancelAndConfirmView)
                // When
                clickConfirm(cancelAndConfirmView)
                // Then
                assertThat(confirmed).isTrue
                assertThat(cancelled).isFalse
            }
    }
}
