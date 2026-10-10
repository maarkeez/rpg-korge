package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.descendantsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleLayoutTest : ViewsForTesting() {
    @Nested
    inner class Battlefield {
        @Test
        fun `should cover at least 85 percent of the screen height when no unit is selected`() {
            // Given
            val screenHeight = BattleLayout.SCREEN_HEIGHT.toDouble()
            // When
            val battlefieldHeight = BattleLayout.BATTLEFIELD_HEIGHT.toDouble()
            // Then
            assertThat(battlefieldHeight / screenHeight).isGreaterThanOrEqualTo(0.85)
        }

        @Test
        fun `should end at the bottom of the screen when the layout is defined`() {
            // Given
            val battlefieldBottom = BattleLayout.BATTLEFIELD_Y + BattleLayout.BATTLEFIELD_HEIGHT
            // When
            val screenBottom = BattleLayout.SCREEN_HEIGHT
            // Then
            assertThat(battlefieldBottom).isEqualTo(screenBottom)
        }
    }

    @Nested
    inner class Sheet {
        @Test
        fun `should be at most 260 points tall and above the action bar when the layout is defined`() {
            // Given
            val sheetHeight = BattleLayout.SHEET_HEIGHT
            // When
            val sheetBottom = BattleLayout.SHEET_Y + sheetHeight
            // Then
            assertThat(sheetHeight).isLessThanOrEqualTo(260)
            assertThat(sheetBottom).isEqualTo(BattleLayout.ACTION_BAR_Y)
        }

        @Test
        fun `should contain ability buttons of at least the minimum touch size when battle unit info is displayed`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                // When
                val abilityButtons = battleUnitInfoView.descendantsWith { it is AbilityButtonView }
                // Then
                assertThat(abilityButtons).isNotEmpty
                abilityButtons.forEach { abilityButton ->
                    assertThat(abilityButton.width).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                    assertThat(abilityButton.height).isGreaterThanOrEqualTo(BattleLayout.MIN_TOUCH_TARGET.toDouble())
                }
            }
    }
}
