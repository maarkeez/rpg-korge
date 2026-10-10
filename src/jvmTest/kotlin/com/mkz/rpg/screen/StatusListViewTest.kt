package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIText
import korlibs.korge.view.Image
import korlibs.korge.view.descendantsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatusListViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should show the turns left when the status is an on turn started effect`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, 54))
                // When
                statusListView.display(listOf(StatusListView.Status(effectId = "venom-damage", turnsLeft = 3)))
                // Then
                assertThat(labels(statusListView)).containsExactly("3 turns")
                assertThat(statusListView.descendantsWith { it.name == StatusListView.STATUS_ON_DEATH_GLYPH }).isEmpty()
            }

        @Test
        fun `should show a singular label when one turn is left`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, 54))
                // When
                statusListView.display(listOf(StatusListView.Status(effectId = "venom-damage", turnsLeft = 1)))
                // Then
                assertThat(labels(statusListView)).containsExactly("1 turn")
            }

        @Test
        fun `should show the on death glyph and label when the status is an on defeated effect`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, 54))
                // When
                statusListView.display(listOf(StatusListView.Status(effectId = "venom-on-death", turnsLeft = null)))
                // Then
                assertThat(labels(statusListView)).containsExactly("On death")
                assertThat(statusListView.descendantsWith { it.name == StatusListView.STATUS_ON_DEATH_GLYPH }).hasSize(1)
            }

        @Test
        fun `should draw the status icon at twice its art size when a status is displayed`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, StatusListView.ROW_HEIGHT))
                // When
                statusListView.display(listOf(StatusListView.Status(effectId = "venom-damage", turnsLeft = 2)))
                // Then
                val icon = statusListView.descendantsWith { it is Image }.single()
                assertThat(icon.scaledWidth).isEqualTo(32.0)
                assertThat(icon.scaledHeight).isEqualTo(32.0)
            }

        @Test
        fun `should collapse the extra statuses into a more row when there are more than three statuses`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, 54))
                val statuses = List(5) { StatusListView.Status(effectId = "effect-$it", turnsLeft = 2) }
                // When
                statusListView.display(statuses)
                // Then
                assertThat(statusListView.rowCount).isEqualTo(2)
                val overflow = statusListView.descendantsWith { it.name == StatusListView.STATUS_OVERFLOW_LABEL }.single() as UIText
                assertThat(overflow.text).isEqualTo("+3 more")
            }

        @Test
        fun `should not be visible when there are no statuses`() =
            viewsTest {
                // Given
                val statusListView = StatusListView(korlibs.math.geom.Size(390, 54))
                // When
                statusListView.display(emptyList())
                // Then
                assertThat(statusListView.visible).isFalse()
            }
    }

    private fun labels(statusListView: StatusListView) =
        statusListView
            .descendantsWith { it.name == StatusListView.STATUS_TURNS_LABEL }
            .map { (it as UIText).text }
}
