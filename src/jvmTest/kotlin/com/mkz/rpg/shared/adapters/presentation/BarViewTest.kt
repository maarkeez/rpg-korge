package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BarViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should not be visible when is initialized`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                // Then
                barView.isVisibleToUser().shouldBeFalse()
                barView.isProgressBarShown().shouldBeFalse()
                barView.isLabelShown().shouldBeFalse()
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                barView.isVisibleToUser().shouldBeTrue()
                barView.isProgressBarShown().shouldBeTrue()
                barView.isLabelShown().shouldBeTrue()
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                barView.labelText() shouldBe "50 / 100"
            }

        @Test
        fun `should modify progress bar when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                val remaining = 50
                val maximum = 100
                val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                barView.progressPercentage() shouldBe percentage
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should be visible when was hide`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                barView.display(remaining = 50, maximum = 100)
                // When
                barView.hide()
                // Then
                barView.isVisibleToUser().shouldBeFalse()
                barView.isProgressBarShown().shouldBeFalse()
                barView.isLabelShown().shouldBeFalse()
            }
    }
}
