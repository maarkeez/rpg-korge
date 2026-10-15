package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class BattleUnitInfoViewTest : ViewsForTesting() {
    @Test
    fun `Init should not be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            // When
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit().toDto()
            val battleUnit = battleUnit(unit = unit).toDto()
            // When
            battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display the remaining steps and casts out of their maximum when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit(movementRange = 3).toDto()
            val battleUnit = battleUnit(unit = unit).move(fromRow = 0, fromColumn = 0, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1))).toDto()
            // When
            battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
            // Then
            battleUnitInfoView.remainingSteps() shouldBe 2
            battleUnitInfoView.maximumSteps() shouldBe 3
            battleUnitInfoView.remainingCasts() shouldBe 1
            battleUnitInfoView.maximumCasts() shouldBe 1
        }

    @Test
    fun `Display should display the steps in white and the casts in gold when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit().toDto()
            val battleUnit = battleUnit(unit = unit).toDto()
            // When
            battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
            // Then
            battleUnitInfoView.movementPointColors().shouldContainExactly(BattleUnitInfoView.MOVEMENT_POINTS_COLOR)
            battleUnitInfoView.castPointColors().shouldContainExactly(BattleUnitInfoView.CAST_POINTS_COLOR)
        }

    @Test
    fun `Display should display no remaining casts when the battle unit has cast this turn`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit().toDto()
            val battleUnit = battleUnit(unit = unit).toDto().let { dto -> dto.copy(remainingTurnActions = dto.remainingTurnActions.copy(remainingCasts = 0)) }
            // When
            battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
            // Then
            battleUnitInfoView.remainingCasts() shouldBe 0
            battleUnitInfoView.maximumCasts() shouldBe 1
        }

    @Test
    fun `DisplayAbilitySelected should select ability button when ability is selected`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val selectedAbilityIndex = 0
            // When
            battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
            // Then
            val abilityButtons = battleUnitInfoView.abilityButtons()
            val selectedAbilityButton = abilityButtons[selectedAbilityIndex]
            selectedAbilityButton.isSelected().shouldBeTrue()
        }

    @Test
    fun `DisplayAbilitySelected should unselect other ability buttons when ability is selected`() =
        sharedViewsTest {
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
                abilityButton.isSelected() shouldBe isSelected
            }
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit().toDto()
            val battleUnit = battleUnit(unit = unit).toDto()
            battleUnitInfoView.display(battleUnit = battleUnit, unit = unit)
            // When
            battleUnitInfoView.hide()
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `DisplayBag should display the bag and open the inventory when the bag is clicked`() =
        sharedViewsTest {
            // Given
            var bagSelected = false
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            val unit = unit().toDto()
            battleUnitInfoView.display(battleUnit = battleUnit(unit = unit).toDto(), unit = unit)
            addChild(battleUnitInfoView)
            battleUnitInfoView.displayBag(onBagSelected = { bagSelected = true })
            // When
            clickBag(battleUnitInfoView)
            // Then
            battleUnitInfoView.isBagDisplayed().shouldBeTrue()
            bagSelected.shouldBeTrue()
        }

    @Test
    fun `HideBag should not display the bag when the bag is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader())
            battleUnitInfoView.displayBag(onBagSelected = {})
            // When
            battleUnitInfoView.hideBag()
            // Then
            battleUnitInfoView.isBagDisplayed().shouldBeFalse()
        }
}
