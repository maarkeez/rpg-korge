package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnitNameViewTest : ViewsForTesting() {
    private val unitName = "Unit name"

    @Nested
    inner class Init {
        @Test
        fun `should be visible when is initialized`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                // When
                // Then
                assertThat(unitNameView.isVisibleToUser()).isTrue
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                unitNameView.hide()
                // When
                unitNameView.display(unitName = unitName)
                // Then
                assertThat(unitNameView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify unit name label when is displayed`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                // When
                unitNameView.display(unitName = unitName)
                // Then
                assertThat(unitNameView.nameText()).isEqualTo(unitName)
            }
    }

    @Nested
    inner class AlignTextToRight {
        @Test
        fun `should align text to right when is aligned`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                // When
                unitNameView.alignTextToRight()
                // Then
                assertThat(unitNameView.isAlignedToRight()).isTrue
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                unitNameView.display(unitName = unitName)
                // When
                unitNameView.hide()
                // Then
                assertThat(unitNameView.isVisibleToUser()).isFalse
            }
    }
}
