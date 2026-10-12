package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ConfirmButtonTest : ViewsForTesting() {
    @Test
    fun `Init should display confirm text when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val confirmButton = ConfirmButton(onConfirmed = {})
            // Then
            confirmButton.buttonText() shouldBe "Confirm"
        }

    @Test
    fun `ButtonClick should confirm when button is clicked`() =
        sharedViewsTest {
            // Given
            var confirmed = false
            val confirmButton = ConfirmButton(onConfirmed = { confirmed = true })
            addChild(confirmButton)
            // When
            clickConfirm(confirmButton)
            // Then
            confirmed.shouldBeTrue()
        }
}
