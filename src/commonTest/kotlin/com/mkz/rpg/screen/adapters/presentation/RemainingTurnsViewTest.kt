package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class RemainingTurnsViewTest : ViewsForTesting() {
    @Test
    fun `Init should not be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val remainingTurnsView = RemainingTurnsView(Size(width = 150.0, height = 14.0), TextsMother.texts())
            // When
            // Then
            remainingTurnsView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `Display should tell the turns left before vanishing when is displayed`() =
        sharedViewsTest {
            // Given
            val remainingTurnsView = RemainingTurnsView(Size(width = 150.0, height = 14.0), TextsMother.texts())
            // When
            remainingTurnsView.display(remainingTurns = 3)
            // Then
            remainingTurnsView.isVisibleToUser().shouldBeTrue()
            remainingTurnsView.remainingTurnsText() shouldBe "Vanishes in 3 turns"
        }

    @Test
    fun `Display should tell a single turn left when the current turn is the last one`() =
        sharedViewsTest {
            // Given
            val remainingTurnsView = RemainingTurnsView(Size(width = 150.0, height = 14.0), TextsMother.texts())
            // When
            remainingTurnsView.display(remainingTurns = 1)
            // Then
            remainingTurnsView.remainingTurnsText() shouldBe "Vanishes in 1 turn"
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val remainingTurnsView = RemainingTurnsView(Size(width = 150.0, height = 14.0), TextsMother.texts())
            remainingTurnsView.display(remainingTurns = 3)
            // When
            remainingTurnsView.hide()
            // Then
            remainingTurnsView.isVisibleToUser().shouldBeFalse()
        }
}
