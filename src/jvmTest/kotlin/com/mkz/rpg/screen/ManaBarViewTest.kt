package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ManaBarViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should not be visible when is initialized`() =
            viewsTest {
                // Given
                val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
                // When
                // Then
                assertThat(manaBarView.isVisibleToUser()).isFalse
                assertThat(manaBarView.progressBar.isVisibleToUser()).isFalse
                assertThat(manaBarView.label.isVisibleToUser()).isFalse
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
                // When
                manaBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(manaBarView.isVisibleToUser()).isTrue
                assertThat(manaBarView.progressBar.isVisibleToUser()).isTrue
                assertThat(manaBarView.label.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
                // When
                manaBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(manaBarView.label.text).isEqualTo("50 / 100")
            }

        @Test
        fun `should modify progress bar when is displayed`() =
            viewsTest {
                // Given
                val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
                val remaining = 50
                val maximum = 100
                val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
                // When
                manaBarView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(manaBarView.progressBar.current).isEqualTo(percentage)
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val manaBarView = ManaBarView(Size(width = 100.00, height = 50.00))
                manaBarView.display(remaining = 50, maximum = 100)
                // When
                manaBarView.hide()
                // Then
                assertThat(manaBarView.isVisibleToUser()).isFalse
                assertThat(manaBarView.progressBar.isVisibleToUser()).isFalse
                assertThat(manaBarView.label.isVisibleToUser()).isFalse
            }
    }
}
