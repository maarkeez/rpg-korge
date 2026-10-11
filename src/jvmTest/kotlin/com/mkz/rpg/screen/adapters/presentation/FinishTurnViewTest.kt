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
    }
}
