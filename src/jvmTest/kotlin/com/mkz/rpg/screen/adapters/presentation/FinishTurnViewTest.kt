package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FinishTurnViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should display finish turn text when is initialized`() =
            viewsTest {
                // Given
                // When
                val finishTurnView = FinishTurnView(onTurnFinished = {})
                // Then
                assertThat(finishTurnView.buttonText()).isEqualTo("Finish turn")
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should finish turn when button is clicked`() =
            viewsTest {
                // Given
                var turnFinished = false
                val finishTurnView = FinishTurnView(onTurnFinished = { turnFinished = true })
                addChild(finishTurnView)
                // When
                clickFinishTurn(finishTurnView)
                // Then
                assertThat(turnFinished).isTrue
            }

        @Test
        fun `should not finish turn when button is clicked and is disabled`() =
            viewsTest {
                // Given
                var turnFinished = false
                val finishTurnView = FinishTurnView(onTurnFinished = { turnFinished = true })
                finishTurnView.isEnabled = false
                addChild(finishTurnView)
                // When
                clickFinishTurn(finishTurnView)
                // Then
                assertThat(turnFinished).isFalse
            }
    }

    @Nested
    inner class IsEnabled {
        @Test
        fun `should be enabled when is initialized`() =
            viewsTest {
                // Given
                // When
                val finishTurnView = FinishTurnView(onTurnFinished = {})
                // Then
                assertThat(finishTurnView.isButtonEnabled()).isTrue
            }

        @Test
        fun `should be disabled when is disabled`() =
            viewsTest {
                // Given
                val finishTurnView = FinishTurnView(onTurnFinished = {})
                // When
                finishTurnView.isEnabled = false
                // Then
                assertThat(finishTurnView.isButtonEnabled()).isFalse
            }
    }
}
