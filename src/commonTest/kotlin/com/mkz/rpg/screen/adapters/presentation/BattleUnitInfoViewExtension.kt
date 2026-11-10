package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

fun BattleUnitInfoView.abilityButtons(): List<AbilityButtonView> = abilityButtons.toList()

fun BattleUnitInfoView.remainingSteps(): Int = movementPointsView.filledPoints()

fun BattleUnitInfoView.maximumSteps(): Int = movementPointsView.filledPoints() + movementPointsView.hollowPoints()

fun BattleUnitInfoView.remainingCasts(): Int = castPointsView.filledPoints()

fun BattleUnitInfoView.maximumCasts(): Int = castPointsView.filledPoints() + castPointsView.hollowPoints()

fun BattleUnitInfoView.remainingTurnsText(): String? = if (remainingSummonDurationView.visible) remainingSummonDurationView.remainingTurnsText() else null

fun BattleUnitInfoView.isRemainingTurnsAfterName(): Boolean = remainingSummonDurationView.x > nameText.x + nameText.width && remainingSummonDurationView.y == nameText.y - 1

fun BattleUnitInfoView.portraitRingColor() = unitPortraitView.ringColor()

fun BattleUnitInfoView.movementPointColors() = movementPointsView.pointColors()

fun BattleUnitInfoView.castPointColors() = castPointsView.pointColors()

fun BattleUnitInfoView.selectedAbilityIndexes(): List<Int> = abilityButtons.indices.filter { index -> abilityButtons[index].isSelected() }

fun BattleUnitInfoView.isAbilityInfoDisplayed(): Boolean = visible && abilityInfoView.visible

fun BattleUnitInfoView.displayedAbilityName(): String = abilityInfoView.abilityName()

fun BattleUnitInfoView.isAbilityInfoUnderAbilityButtons(): Boolean = abilityInfoView.getGlobalBounds().top >= abilityButtons.maxOf { button -> button.getGlobalBounds().bottom }

fun BattleUnitInfoView.isInventoryButtonDisplayed(): Boolean = header.visible && inventoryButton.visible && inventoryButton.findViewByName(InventoryButtonView.CHEST) != null

fun BattleUnitInfoView.isHeaderCollapsed(): Boolean = isFocused && strip.visible && !header.visible

fun BattleUnitInfoView.isNameDisplayed(): Boolean = header.visible && nameText.visible && nameText.text.isNotEmpty()

fun BattleUnitInfoView.areEffectsDisplayed(): Boolean = header.visible && effectsView.visible

fun BattleUnitInfoView.areAbilityButtonsUnderHeader(): Boolean = body.y == if (isFocused) BattleUnitInfoView.FOCUSED_ABILITIES_Y else BattleUnitInfoView.ABILITIES_Y

suspend fun ViewsForTesting.clickInventoryButton(battleUnitInfoView: BattleUnitInfoView) = clickOn(battleUnitInfoView.inventoryButton)

suspend fun ViewsForTesting.clickAbility(
    battleUnitInfoView: BattleUnitInfoView,
    index: Int,
) = clickAbilityButton(battleUnitInfoView.abilityButtons[index])
