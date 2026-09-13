package com.mkz.rpg.screen

import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIText
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
                val unitNameLabel = unitNameView.children[0] as UIText
                assertThat(unitNameLabel.text).isEqualTo(unitName)
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
                val unitNameLabel = unitNameView.children[0] as UIText
                assertThat(unitNameLabel.styles.textAlignment).isEqualTo(TextAlignment.RIGHT)
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
