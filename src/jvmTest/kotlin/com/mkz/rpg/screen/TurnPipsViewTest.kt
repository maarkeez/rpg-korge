package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TurnPipsViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should fill the available pips and hollow the spent ones when some actions were used`() =
            viewsTest {
                // Given
                val turnPipsView = TurnPipsView(Size(281.5, 14))
                // When
                turnPipsView.display(remainingSteps = 1, maximumSteps = 3, remainingCasts = 0, maximumCasts = 1)
                // Then
                assertThat(turnPipsView.filledMovePips).isEqualTo(1)
                assertThat(turnPipsView.hollowMovePips).isEqualTo(2)
                assertThat(turnPipsView.filledCastPips).isZero()
                assertThat(turnPipsView.hollowCastPips).isEqualTo(1)
            }

        @Test
        fun `should fill every pip when no action was used`() =
            viewsTest {
                // Given
                val turnPipsView = TurnPipsView(Size(281.5, 14))
                // When
                turnPipsView.display(remainingSteps = 3, maximumSteps = 3, remainingCasts = 1, maximumCasts = 1)
                // Then
                assertThat(turnPipsView.filledMovePips).isEqualTo(3)
                assertThat(turnPipsView.hollowMovePips).isZero()
                assertThat(turnPipsView.filledCastPips).isEqualTo(1)
            }

        @Test
        fun `should replace the previous pips when it is displayed again`() =
            viewsTest {
                // Given
                val turnPipsView = TurnPipsView(Size(281.5, 14))
                turnPipsView.display(remainingSteps = 3, maximumSteps = 3, remainingCasts = 1, maximumCasts = 1)
                // When
                turnPipsView.display(remainingSteps = 0, maximumSteps = 3, remainingCasts = 1, maximumCasts = 1)
                // Then
                assertThat(turnPipsView.filledMovePips).isZero()
                assertThat(turnPipsView.hollowMovePips).isEqualTo(3)
            }
    }
}
