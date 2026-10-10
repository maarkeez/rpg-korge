package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.screen.CastPreviewSummary.Line
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AttackPreviewViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                // When
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = emptyList())
                // Then
                assertThat(attackPreviewView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should show the cooldown the ability will set when the ability has a cooldown`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                // When
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 2, lines = emptyList())
                // Then
                assertThat(attackPreviewView.cooldownLineText).isEqualTo("Cooldown: 2 turns")
            }

        @Test
        fun `should show one line per target when the preview has several targets`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                val lines = listOf(Line("Rat: 20 -> 10 HP", Line.Kind.TARGET), Line("Rat: 20 -> 10 HP", Line.Kind.TARGET))
                // When
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = lines)
                // Then
                assertThat(attackPreviewView.displayedLines).containsExactly("Rat: 20 -> 10 HP", "Rat: 20 -> 10 HP")
            }

        @Test
        fun `should collapse the extra lines when the preview has more lines than fit`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                val lines = List(AttackPreviewView.MAX_LINES + 2) { index -> Line("Target $index", Line.Kind.TARGET) }
                // When
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = lines)
                // Then
                assertThat(attackPreviewView.displayedLines).hasSize(AttackPreviewView.MAX_LINES)
                assertThat(attackPreviewView.displayedLines.last()).isEqualTo("+3 more")
            }

        @Test
        fun `should replace the previous lines when it is displayed again`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = listOf(Line("Rat A", Line.Kind.TARGET)))
                // When
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = listOf(Line("Rat B", Line.Kind.TARGET)))
                // Then
                assertThat(attackPreviewView.displayedLines).containsExactly("Rat B")
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val attackPreviewView = AttackPreviewView()
                attackPreviewView.loadAssets()
                val casterUnit = unit(id = "knight").toDto()
                val casterBattleUnit = battleUnit(unit = casterUnit).toDto()
                attackPreviewView.display(casterBattleUnit, casterUnit, manaAfter = 0, cooldownAfter = 0, lines = emptyList())
                // When
                attackPreviewView.hide()
                // Then
                assertThat(attackPreviewView.isVisibleToUser()).isFalse
            }
    }
}
