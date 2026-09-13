package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HealthBarViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should not be visible when is initialized`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                // Then
                assertThat(healthBarView.isVisibleToUser()).isFalse
                assertThat(healthBarView.progressBar.isVisibleToUser()).isFalse
                assertThat(healthBarView.label.isVisibleToUser()).isFalse
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(healthBarView.isVisibleToUser()).isTrue
                assertThat(healthBarView.progressBar.isVisibleToUser()).isTrue
                assertThat(healthBarView.label.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(healthBarView.label.text).isEqualTo("50 / 100")
            }

        @Test
        fun `should modify progress bar when is displayed`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                val remaining = 50
                val maximum = 100
                val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
                // When
                healthBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(healthBarView.progressBar.current).isEqualTo(percentage)
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val healthBarView = HealthBarView(Size(width = 100.00, height = 50.00))
                healthBarView.display(remaining = 50, maximum = 100)
                // When
                healthBarView.hide()
                // Then
                assertThat(healthBarView.isVisibleToUser()).isFalse
                assertThat(healthBarView.progressBar.isVisibleToUser()).isFalse
                assertThat(healthBarView.label.isVisibleToUser()).isFalse
            }
    }
}
