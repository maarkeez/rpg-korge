package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class BarViewTest : ViewsForTesting() {
    @Test
    fun `Init should not be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
            // When
            // Then
            barView.isVisibleToUser().shouldBeFalse()
            barView.isProgressBarShown().shouldBeFalse()
            barView.isLabelShown().shouldBeFalse()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
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
    fun `Display should modify label when is displayed`() =
        sharedViewsTest {
            // Given
            val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
            // When
            barView.display(remaining = 50, maximum = 100)
            // Then
            barView.labelText() shouldBe "50 / 100"
        }

    @Test
    fun `Display should modify progress bar when is displayed`() =
        sharedViewsTest {
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

    @Test
    fun `Hide should be visible when was hide`() =
        sharedViewsTest {
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
