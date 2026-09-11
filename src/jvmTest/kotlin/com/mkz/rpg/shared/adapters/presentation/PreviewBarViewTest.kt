package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PreviewBarViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should be visible when is initialized`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                // When
                // Then
                assertThat(previewBarView.isVisibleToUser()).isTrue
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                previewBarView.hide()
                // When
                previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                assertThat(previewBarView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                // When
                previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                assertThat(previewBarView.label.text).isEqualTo("50 / 100")
            }

        @Test
        fun `should modify filled width when is displayed`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                val remainingAfter = 50
                val maximum = 100
                val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
                // When
                previewBarView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
                // Then
                assertThat(previewBarView.filled.width).isEqualTo(size.width * filledPercentage)
            }

        @Test
        fun `should modify preview width when is displayed`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                val remainingBefore = 75
                val maximum = 100
                val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
                // When
                previewBarView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
                // Then
                assertThat(previewBarView.preview.width).isEqualTo(size.width * previewPercentage)
            }

        @Test
        fun `should set filled width to zero when remaining after is negative`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                // When
                previewBarView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
                // Then
                assertThat(previewBarView.filled.width).isEqualTo(0.00)
                assertThat(previewBarView.label.text).isEqualTo("0 / 100")
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val previewBarView = PreviewBarView(size, Colors.RED, Colors.GREEN)
                previewBarView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // When
                previewBarView.hide()
                // Then
                assertThat(previewBarView.isVisibleToUser()).isFalse
            }
    }
}
