package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Container
import korlibs.korge.view.View
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
                val battleUnitInfoView = BattleUnitInfoView()
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
                val battleUnitInfoView = BattleUnitInfoView()
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
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val selectedAbilityIndex = 0
                // When
                battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
                // Then
                val abilityButtons = battleUnitInfoView.allViews().filterIsInstance<AbilityButtonView>()
                val selectedAbilityButton = abilityButtons[selectedAbilityIndex]
                assertThat(selectedAbilityButton.findViewByName(AbilityButtonView.ABILITY_SELECTION)).isNotNull
            }

        @Test
        fun `should unselect other ability buttons when ability is selected`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val abilityButtons = battleUnitInfoView.allViews().filterIsInstance<AbilityButtonView>()
                abilityButtons.forEach { abilityButton -> abilityButton.select() }
                val selectedAbilityIndex = 0
                // When
                battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
                // Then
                abilityButtons.forEachIndexed { index, abilityButton ->
                    val isSelected = index == selectedAbilityIndex
                    assertThat(abilityButton.findViewByName(AbilityButtonView.ABILITY_SELECTION) != null).isEqualTo(isSelected)
                }
            }
    }

    @Nested
    inner class Hide {
        @Test
        fun `should not be visible when is hidden`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
                // When
                battleUnitInfoView.hide()
                // Then
                assertThat(battleUnitInfoView.isVisibleToUser()).isFalse
            }
    }

    @Nested
    inner class DisplayInteractive {
        @Test
        fun `should show ability buttons when the battle unit is commanded by the player`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                // When
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit, interactive = true)
                // Then
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isEqualTo(battleUnit.abilityCooldowns.size)
                assertThat(battleUnitInfoView.readOnlyAbilityIconCount).isZero()
            }

        @Test
        fun `should show read only ability icons and no ability buttons when the battle unit is inspected`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                // When
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit, interactive = false)
                // Then
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isZero()
                assertThat(battleUnitInfoView.readOnlyAbilityIconCount).isEqualTo(battleUnit.abilityCooldowns.size)
            }

        @Test
        fun `should clear the ability buttons when an ally is displayed after an inspected enemy`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val unit = unit().toDto()
                val battleUnit = battleUnit(unit = unit).toDto()
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit, interactive = false)
                // When
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit, interactive = true)
                // Then
                assertThat(battleUnitInfoView.readOnlyAbilityIconCount).isZero()
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isEqualTo(battleUnit.abilityCooldowns.size)
            }
    }

    @Nested
    inner class DisplayStatuses {
        @Test
        fun `should list one row per status when the battle unit has ongoing effects`() =
            viewsTest {
                // Given
                val battleUnitInfoView = BattleUnitInfoView()
                battleUnitInfoView.loadAssets()
                val unit = unit().toDto()
                val battleUnit =
                    battleUnit(unit = unit)
                        .receiveOnTurnStartedEffect(effectId = "venom-damage", turnsLeft = 3)
                        .receiveOnDefeatedEffect(effectId = "venom-on-death")
                        .toDto()
                // When
                battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
                // Then
                assertThat(battleUnitInfoView.displayedStatusRows).isEqualTo(2)
            }
    }
}

private fun Container.allViews(): List<View> {
    val views = mutableListOf<View>(this)
    children.forEach { child ->
        if (child is Container) views += child.allViews()
    }
    return views
}
