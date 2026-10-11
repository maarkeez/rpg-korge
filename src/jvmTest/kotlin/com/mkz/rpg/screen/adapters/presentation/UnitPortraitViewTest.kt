package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
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
                assertThat(unitPortraitView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display knight portrait when knight unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "knight")
                // Then
                assertThat(unitPortraitView.isPortraitDisplayed()).isTrue
            }

        @Test
        fun `should display rat portrait when rat unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "rat")
                // Then
                assertThat(unitPortraitView.isPortraitDisplayed()).isTrue
            }

        @Test
        fun `should not display portrait when unknown unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), loadedResourcesLoader())
                // When
                unitPortraitView.display(unitId = "unknown-unit")
                // Then
                assertThat(unitPortraitView.isPortraitDisplayed()).isFalse
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
                assertThat(unitPortraitView.isVisibleToUser()).isFalse
            }
    }
}
