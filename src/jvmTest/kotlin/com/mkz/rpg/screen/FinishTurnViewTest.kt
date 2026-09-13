package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FinishTurnViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should be visible when is initialized`() =
            viewsTest {
                // Given
                val finishTurnView = FinishTurnView()
                // When
                // Then
                assertThat(finishTurnView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display finish turn button when is initialized`() =
            viewsTest {
                // Given
                val finishTurnView = FinishTurnView()
                // When
                // Then
                val finishTurnButton = finishTurnView.children[0] as UIButton
                assertThat(finishTurnButton.text).isEqualTo("Finish turn")
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should notify delegate when finish turn button is clicked`() =
            viewsTest {
                // Given
                val finishTurnView = FinishTurnView()
                val delegate = mock<FinishTurnView.Delegate>()
                finishTurnView.setDelegate(delegate)
                addChild(finishTurnView)
                val finishTurnButton = finishTurnView.children[0] as UIButton
                // When
                finishTurnButton.simulateClick()
                // Then
                verify(delegate).finishTurn()
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val finishTurnView = FinishTurnView()
                // When
                finishTurnView.hide()
                // Then
                assertThat(finishTurnView.isVisibleToUser()).isFalse
            }
    }
}
