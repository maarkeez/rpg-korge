package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class UnitPortraitViewTest : ViewsForTesting() {
    @Test
    fun `Display should be visible when is displayed`() =
        viewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "knight")
            // Then
            unitPortraitView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display knight portrait when knight unit is displayed`() =
        viewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "knight")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeTrue()
        }

    @Test
    fun `Display should display rat portrait when rat unit is displayed`() =
        viewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "rat")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeTrue()
        }

    @Test
    fun `Display should not display portrait when unknown unit is displayed`() =
        viewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            // When
            unitPortraitView.display(unitId = "unknown-unit")
            // Then
            unitPortraitView.isPortraitDisplayed().shouldBeFalse()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        viewsTest {
            // Given
            val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
            unitPortraitView.display(unitId = "knight")
            // When
            unitPortraitView.hide()
            // Then
            unitPortraitView.isVisibleToUser().shouldBeFalse()
        }
}
