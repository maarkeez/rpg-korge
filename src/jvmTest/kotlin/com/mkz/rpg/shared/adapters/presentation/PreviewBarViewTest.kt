package com.mkz.rpg.shared.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Test

class PreviewBarViewTest : ViewsForTesting() {
    @Test
    fun `Init should be visible when is initialized`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            // Then
            previewBarView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            previewBarView.hide()
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            previewBarView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should modify label when is displayed`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // Then
            previewBarView.labelText() shouldBe "50 / 100"
        }

    @Test
    fun `Display should modify filled width when is displayed`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            val remainingAfter = 50
            val maximum = 100
            val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
            // Then
            previewBarView.filledWidth() shouldBe size.width * filledPercentage
        }

    @Test
    fun `Display should modify preview width when is displayed`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            val remainingBefore = 75
            val maximum = 100
            val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
            // When
            previewBarView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
            // Then
            previewBarView.previewWidth() shouldBe size.width * previewPercentage
        }

    @Test
    fun `Display should set filled width to zero when remaining after is negative`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            // When
            previewBarView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
            // Then
            previewBarView.filledWidth() shouldBe 0.00
            previewBarView.labelText() shouldBe "0 / 100"
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        viewsTest {
            // Given
            val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
            previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
            // When
            previewBarView.hide()
            // Then
            previewBarView.isVisibleToUser().shouldBeFalse()
        }
}
