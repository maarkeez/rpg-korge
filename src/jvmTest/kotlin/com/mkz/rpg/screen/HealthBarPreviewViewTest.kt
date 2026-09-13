package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HealthBarPreviewViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should be visible when is initialized`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                // When
                // Then
                assertThat(healthBarPreviewView.isVisibleToUser()).isTrue
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                healthBarPreviewView.hide()
                // When
                healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                assertThat(healthBarPreviewView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                // When
                healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // Then
                assertThat(healthBarPreviewView.label.text).isEqualTo("50 / 100")
            }

        @Test
        fun `should modify filled width when is displayed`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                val remainingAfter = 50
                val maximum = 100
                val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
                // When
                healthBarPreviewView.display(remainingBefore = 100, remainingAfter = remainingAfter, maximum = maximum)
                // Then
                assertThat(healthBarPreviewView.filled.width).isEqualTo(size.width * filledPercentage)
            }

        @Test
        fun `should modify preview width when is displayed`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                val remainingBefore = 75
                val maximum = 100
                val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
                // When
                healthBarPreviewView.display(remainingBefore = remainingBefore, remainingAfter = 50, maximum = maximum)
                // Then
                assertThat(healthBarPreviewView.preview.width).isEqualTo(size.width * previewPercentage)
            }

        @Test
        fun `should set filled width to zero when remaining after is negative`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                // When
                healthBarPreviewView.display(remainingBefore = 100, remainingAfter = -10, maximum = 100)
                // Then
                assertThat(healthBarPreviewView.filled.width).isEqualTo(0.00)
                assertThat(healthBarPreviewView.label.text).isEqualTo("0 / 100")
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val healthBarPreviewView = HealthBarPreviewView(size)
                healthBarPreviewView.display(remainingBefore = 100, remainingAfter = 50, maximum = 100)
                // When
                healthBarPreviewView.hide()
                // Then
                assertThat(healthBarPreviewView.isVisibleToUser()).isFalse
            }
    }
}
