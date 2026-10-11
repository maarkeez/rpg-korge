package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlayerCallToActionViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayFinishTurn {
        @Test
        fun `should display finish turn when finish turn is displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                // When
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // Then
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isTrue
                assertThat(playerCallToActionView.isDisplayingCancelAndConfirm()).isFalse
            }

        @Test
        fun `should finish turn when finish turn is clicked`() =
            viewsTest {
                // Given
                var turnFinished = false
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = { turnFinished = true })
                addChild(playerCallToActionView)
                // When
                clickFinishTurn(playerCallToActionView)
                // Then
                assertThat(turnFinished).isTrue
            }
    }

    @Nested
    inner class SetFinishTurnEnabled {
        @Test
        fun `should display the finish turn disabled when finish turn is disabled`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // When
                playerCallToActionView.setFinishTurnEnabled(false)
                // Then
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isTrue
                assertThat(playerCallToActionView.isFinishTurnEnabled()).isFalse
            }

        @Test
        fun `should display the finish turn disabled when it was disabled before being displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.setFinishTurnEnabled(false)
                // When
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // Then
                assertThat(playerCallToActionView.isFinishTurnEnabled()).isFalse
            }

        @Test
        fun `should display the finish turn enabled again when finish turn is enabled`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                playerCallToActionView.setFinishTurnEnabled(false)
                // When
                playerCallToActionView.setFinishTurnEnabled(true)
                // Then
                assertThat(playerCallToActionView.isFinishTurnEnabled()).isTrue
            }

        @Test
        fun `should display the finish turn disabled again when it is disabled and cancel is clicked`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                playerCallToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
                playerCallToActionView.setFinishTurnEnabled(false)
                addChild(playerCallToActionView)
                // When
                clickCancel(playerCallToActionView)
                // Then
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isTrue
                assertThat(playerCallToActionView.isFinishTurnEnabled()).isFalse
            }
    }

    @Nested
    inner class DisplayCancelAndConfirm {
        @Test
        fun `should replace finish turn with cancel and confirm when cancel and confirm is displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // When
                playerCallToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
                // Then
                assertThat(playerCallToActionView.isDisplayingCancelAndConfirm()).isTrue
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isFalse
            }

        @Test
        fun `should cancel and display finish turn again when cancel is clicked`() =
            viewsTest {
                // Given
                var cancelled = false
                var confirmed = false
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                playerCallToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
                addChild(playerCallToActionView)
                // When
                clickCancel(playerCallToActionView)
                // Then
                assertThat(cancelled).isTrue
                assertThat(confirmed).isFalse
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isTrue
            }

        @Test
        fun `should confirm and display finish turn again when confirm is clicked`() =
            viewsTest {
                // Given
                var cancelled = false
                var confirmed = false
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                playerCallToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
                addChild(playerCallToActionView)
                // When
                clickConfirm(playerCallToActionView)
                // Then
                assertThat(confirmed).isTrue
                assertThat(cancelled).isFalse
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isTrue
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not display any call to action when is hidden`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // When
                playerCallToActionView.hide()
                // Then
                assertThat(playerCallToActionView.isDisplayingFinishTurn()).isFalse
                assertThat(playerCallToActionView.isDisplayingCancelAndConfirm()).isFalse
            }
    }
}
