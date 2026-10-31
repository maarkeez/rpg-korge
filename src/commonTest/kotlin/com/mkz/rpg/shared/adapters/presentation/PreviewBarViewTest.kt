package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PreviewBarViewTest : ViewsForTesting() {
    @Test
    fun `Init should be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            // Then
            previewBarView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            previewBarView.hide()
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            previewBarView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display the value before and after the change when it is displayed`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            previewBarView.labelText() shouldBe "100 → 50"
        }

    @Test
    fun `Display should fill the bar up to the value after the change when is displayed`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            val remainingAfter = 50
            val maximum = 100
            val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
            // Then
            previewBarView.filledShare() shouldBe filledPercentage
        }

    @Test
    fun `Display should preview the bar up to the value before the change when it decreases`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            val remainingBefore = 75
            val maximum = 100
            val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
            // When
            previewBarView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
            // Then
            previewBarView.previewShare() shouldBe previewPercentage
        }

    @Test
    fun `Display should fill nothing when remaining after is negative`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
            // Then
            previewBarView.filledShare() shouldBe 0.00
            previewBarView.labelText() shouldBe "100 → 0"
        }

    @Test
    fun `Display should draw the lost part lighter when the value decreases`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.PINK)
            // When
            previewBarView.display(remainingBefore = 30, remainingAfter = 18, maximum = 30)
            // Then
            previewBarView.previewColor() shouldBe Colors.PINK
            previewBarView.filledShare() shouldBe 18.0 / 30
        }

    @Test
    fun `Display should draw the gained part green when the value increases`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.PINK)
            // When
            previewBarView.display(remainingBefore = 10, remainingAfter = 20, maximum = 30)
            // Then
            previewBarView.previewColor() shouldBe PreviewBarView.GAIN
            previewBarView.filledShare() shouldBe 10.0 / 30
            previewBarView.previewShare() shouldBe 20.0 / 30
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // When
            previewBarView.hide()
            // Then
            previewBarView.isVisibleToUser().shouldBeFalse()
        }
}
