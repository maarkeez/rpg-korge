package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
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
                assertThat(barView.isVisibleToUser()).isFalse
                assertThat(barView.progressBar.isVisibleToUser()).isFalse
                assertThat(barView.label.isVisibleToUser()).isFalse
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
                assertThat(barView.isVisibleToUser()).isTrue
                assertThat(barView.progressBar.isVisibleToUser()).isTrue
                assertThat(barView.label.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(barView.label.text).isEqualTo("50 / 100")
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
                assertThat(barView.progressBar.current).isEqualTo(percentage)
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
                assertThat(barView.isVisibleToUser()).isFalse
                assertThat(barView.progressBar.isVisibleToUser()).isFalse
                assertThat(barView.label.isVisibleToUser()).isFalse
            }
    }
}
