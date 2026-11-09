package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class FinishPlayerTurnViewTest : ViewsForTesting() {
    @Test
    fun `Init should display finish turn text when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val finishPlayerTurnView = FinishPlayerTurnView(MessagesMother.messages(), onPlayerTurnFinished = {})
            // Then
            finishPlayerTurnView.buttonText() shouldBe "Finish turn"
        }

    @Test
    fun `ButtonClick should finish turn when button is clicked`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val finishPlayerTurnView = FinishPlayerTurnView(MessagesMother.messages(), onPlayerTurnFinished = { turnFinished = true })
            addChild(finishPlayerTurnView)
            // When
            clickFinishPlayerTurn(finishPlayerTurnView)
            // Then
            turnFinished.shouldBeTrue()
        }

    @Test
    fun `ButtonClick should not finish turn when button is clicked and is disabled`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val finishPlayerTurnView = FinishPlayerTurnView(MessagesMother.messages(), onPlayerTurnFinished = { turnFinished = true })
            finishPlayerTurnView.isEnabled = false
            addChild(finishPlayerTurnView)
            // When
            clickFinishPlayerTurn(finishPlayerTurnView)
            // Then
            turnFinished.shouldBeFalse()
        }

    @Test
    fun `IsEnabled should be enabled when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val finishPlayerTurnView = FinishPlayerTurnView(MessagesMother.messages(), onPlayerTurnFinished = {})
            // Then
            finishPlayerTurnView.isButtonEnabled().shouldBeTrue()
        }

    @Test
    fun `IsEnabled should be disabled when is disabled`() =
        sharedViewsTest {
            // Given
            val finishPlayerTurnView = FinishPlayerTurnView(MessagesMother.messages(), onPlayerTurnFinished = {})
            // When
            finishPlayerTurnView.isEnabled = false
            // Then
            finishPlayerTurnView.isButtonEnabled().shouldBeFalse()
            finishPlayerTurnView.isButtonFaded().shouldBeTrue()
        }
}
