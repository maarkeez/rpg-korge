package com.mkz.rpg.screen

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
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                unitPortraitView.loadAssets()
                // When
                unitPortraitView.display(unitId = "knight")
                // Then
                assertThat(unitPortraitView.isVisibleToUser()).isTrue
            }

        @Test
        fun `should display knight portrait when knight unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                unitPortraitView.loadAssets()
                // When
                unitPortraitView.display(unitId = "knight")
                // Then
                val battleUnitPortrait = unitPortraitView.children[0]
                assertThat(battleUnitPortrait.findViewByName("portrait")).isNotNull
            }

        @Test
        fun `should display rat portrait when rat unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                unitPortraitView.loadAssets()
                // When
                unitPortraitView.display(unitId = "rat")
                // Then
                val battleUnitPortrait = unitPortraitView.children[0]
                assertThat(battleUnitPortrait.findViewByName("portrait")).isNotNull
            }

        @Test
        fun `should not display portrait when unknown unit is displayed`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                unitPortraitView.loadAssets()
                // When
                unitPortraitView.display(unitId = "unknown-unit")
                // Then
                val battleUnitPortrait = unitPortraitView.children[0]
                assertThat(battleUnitPortrait.findViewByName("portrait")).isNull()
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                unitPortraitView.loadAssets()
                unitPortraitView.display(unitId = "knight")
                // When
                unitPortraitView.hide()
                // Then
                assertThat(unitPortraitView.isVisibleToUser()).isFalse
            }
    }
}
