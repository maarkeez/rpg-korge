package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnitPortraitViewTest : ViewsForTesting() {
    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "knight")
                // Then
                unitPortraitView.isVisibleToUser().shouldBeTrue()
            }

        @Test
        fun `should display knight portrait when knight unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "knight")
                // Then
                unitPortraitView.isPortraitDisplayed().shouldBeTrue()
            }

        @Test
        fun `should display rat portrait when rat unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "rat")
                // Then
                unitPortraitView.isPortraitDisplayed().shouldBeTrue()
            }

        @Test
        fun `should not display portrait when unknown unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "unknown-unit")
                // Then
                unitPortraitView.isPortraitDisplayed().shouldBeFalse()
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
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
}
