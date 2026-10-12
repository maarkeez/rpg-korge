package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class FinishTurnViewTest : ViewsForTesting() {
    @Test
    fun `Init should display finish turn text when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val finishTurnView = FinishTurnView(onTurnFinished = {})
            // Then
            finishTurnView.buttonText() shouldBe "Finish turn"
        }

    @Test
    fun `ButtonClick should finish turn when button is clicked`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val finishTurnView = FinishTurnView(onTurnFinished = { turnFinished = true })
            addChild(finishTurnView)
            // When
            clickFinishTurn(finishTurnView)
            // Then
            turnFinished.shouldBeTrue()
        }

    @Test
    fun `ButtonClick should not finish turn when button is clicked and is disabled`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val finishTurnView = FinishTurnView(onTurnFinished = { turnFinished = true })
            finishTurnView.isEnabled = false
            addChild(finishTurnView)
            // When
            clickFinishTurn(finishTurnView)
            // Then
            turnFinished.shouldBeFalse()
        }

    @Test
    fun `IsEnabled should be enabled when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val finishTurnView = FinishTurnView(onTurnFinished = {})
            // Then
            finishTurnView.isButtonEnabled().shouldBeTrue()
        }

    @Test
    fun `IsEnabled should be disabled when is disabled`() =
        sharedViewsTest {
            // Given
            val finishTurnView = FinishTurnView(onTurnFinished = {})
            // When
            finishTurnView.isEnabled = false
            // Then
            finishTurnView.isButtonEnabled().shouldBeFalse()
        }
}
