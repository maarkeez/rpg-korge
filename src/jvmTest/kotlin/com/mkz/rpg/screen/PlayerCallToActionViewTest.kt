package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.descendantsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlayerCallToActionViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayFinishTurn {
        @Test
        fun `should display buttons of at least the minimum touch size when finish turn is displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                // When
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // Then
                val buttons = buttonsOf(playerCallToActionView)
                assertThat(buttons).isNotEmpty
                buttons.forEach { button ->
                    assertThat(button.width).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                    assertThat(button.height).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                }
            }

        @Test
        fun `should keep finish turn button inside the screen when finish turn is displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                // When
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // Then
                val button = buttonsOf(playerCallToActionView).single()
                assertThat(button.x).isGreaterThanOrEqualTo(0.0)
                assertThat(button.x + button.width).isLessThanOrEqualTo(BattleLayout.SCREEN_WIDTH.toDouble())
            }
    }

    @Nested
    inner class DisplayCancelAndConfirm {
        @Test
        fun `should display buttons of at least the minimum touch size when cancel and confirm are displayed`() =
            viewsTest {
                // Given
                val playerCallToActionView = PlayerCallToActionView()
                playerCallToActionView.displayFinishTurn(onTurnFinished = {})
                // When
                playerCallToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
                // Then
                val buttons = buttonsOf(playerCallToActionView)
                assertThat(buttons.map { it.text }).containsExactlyInAnyOrder("Cancel", "Confirm")
                buttons.forEach { button ->
                    assertThat(button.width).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                    assertThat(button.height).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                }
            }
    }

    private fun buttonsOf(playerCallToActionView: PlayerCallToActionView): List<UIButton> = playerCallToActionView.descendantsWith { it is UIButton }.map { it as UIButton }
}
