package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
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
                unitNameView.isVisibleToUser().shouldBeTrue()
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
                unitNameView.isVisibleToUser().shouldBeTrue()
            }

        @Test
        fun `should modify unit name label when is displayed`() =
            viewsTest {
                // Given
                val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
                // When
                unitNameView.display(unitName = unitName)
                // Then
                unitNameView.nameText() shouldBe unitName
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
                unitNameView.isAlignedToRight().shouldBeTrue()
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
                unitNameView.isVisibleToUser().shouldBeFalse()
            }
    }
}
