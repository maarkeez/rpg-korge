package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class UnitPortraitViewTest : ViewsForTesting() {
    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "knight")
            // Then
            unitPortraitView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display knight portrait when knight unit is displayed`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "knight")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeTrue()
        }

    @Test
    fun `Display should display rat portrait when rat unit is displayed`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "rat")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeTrue()
        }

    @Test
    fun `Display should not display portrait when unknown unit is displayed`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "unknown-unit")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeFalse()
        }

    @Test
    fun `Display should ring the portrait in the colour given when a ring is given`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "knight", ring = HudStyle.ALLY)
            // Then
            unitPortraitView.ringColor() shouldBe HudStyle.ALLY
        }

    @Test
    fun `Display should draw no ring when no ring is given`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            unitPortraitView.display(unitId = "knight", ring = HudStyle.ENEMY)
            // When
            unitPortraitView.display(unitId = "knight")
            // Then
            unitPortraitView.ringColor().shouldBeNull()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            unitPortraitView.display(unitId = "knight")
            // When
            unitPortraitView.hide()
            // Then
            unitPortraitView.isVisibleToUser().shouldBeFalse()
        }
}
