package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class UnitNameViewTest : ViewsForTesting() {
    private val unitName = "Unit name"

    @Test
    fun `Init should be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
            // When
            // Then
            unitNameView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
            unitNameView.hide()
            // When
            unitNameView.display(unitName = unitName)
            // Then
            unitNameView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should modify unit name label when is displayed`() =
        sharedViewsTest {
            // Given
            val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
            // When
            unitNameView.display(unitName = unitName)
            // Then
            unitNameView.nameText() shouldBe unitName
        }

    @Test
    fun `AlignTextToRight should align text to right when is aligned`() =
        sharedViewsTest {
            // Given
            val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
            // When
            unitNameView.alignTextToRight()
            // Then
            unitNameView.isAlignedToRight().shouldBeTrue()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val unitNameView = UnitNameView(Size(width = 100.00, height = 50.00))
            unitNameView.display(unitName = unitName)
            // When
            unitNameView.hide()
            // Then
            unitNameView.isVisibleToUser().shouldBeFalse()
        }
}
