package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class CancelButtonTest : ViewsForTesting() {
    @Test
    fun `Init should display cancel text when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val cancelButton = CancelButton(onCancelled = {})
            // Then
            cancelButton.buttonText() shouldBe "Cancel"
        }

    @Test
    fun `ButtonClick should cancel when button is clicked`() =
        sharedViewsTest {
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
