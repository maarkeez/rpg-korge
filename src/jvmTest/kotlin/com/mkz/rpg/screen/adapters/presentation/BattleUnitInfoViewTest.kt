package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleUnitInfoViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should not be visible when is initialized`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                // When
                // Then
                assertThat(battleUnitInfoView.isVisibleToUser()).isFalse
            }
    }

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                // When
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
                // Then
                assertThat(battleUnitInfoView.isVisibleToUser()).isTrue
            }
    }

    @Nested
    inner class DisplayAbilitySelected {
        @Test
        fun `should select ability button when ability is selected`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val selectedAbilityIndex = 0
                // When
                battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
                // Then
                val abilityButtons = battleUnitInfoView.abilityButtons()
                val selectedAbilityButton = abilityButtons[selectedAbilityIndex]
                assertThat(selectedAbilityButton.isSelected()).isTrue
            }

        @Test
        fun `should unselect other ability buttons when ability is selected`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val abilityButtons = battleUnitInfoView.abilityButtons()
                abilityButtons.forEach { abilityButton -> abilityButton.select() }
                val selectedAbilityIndex = 0
                // When
                battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
                // Then
                abilityButtons.forEachIndexed { index, abilityButton ->
                    val isSelected = index == selectedAbilityIndex
                    assertThat(abilityButton.isSelected()).isEqualTo(isSelected)
                }
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
                // When
                battleUnitInfoView.hide()
                // Then
                assertThat(battleUnitInfoView.isVisibleToUser()).isFalse
            }
    }
}
