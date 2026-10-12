package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class CancelAndConfirmViewTest : ViewsForTesting() {
    @Test
    fun `CancelClick should only cancel when cancel button is clicked`() =
        viewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val cancelAndConfirmView = CancelAndConfirmView(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(cancelAndConfirmView)
            // When
            clickCancel(cancelAndConfirmView)
            // Then
            cancelled.shouldBeTrue()
            confirmed.shouldBeFalse()
        }

    @Test
    fun `ConfirmClick should only confirm when confirm button is clicked`() =
        viewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val cancelAndConfirmView = CancelAndConfirmView(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(cancelAndConfirmView)
            // When
            clickConfirm(cancelAndConfirmView)
            // Then
            confirmed.shouldBeTrue()
            cancelled.shouldBeFalse()
        }
}
