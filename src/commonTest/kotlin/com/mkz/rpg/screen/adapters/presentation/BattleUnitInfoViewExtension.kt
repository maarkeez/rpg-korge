package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [BattleUnitInfoView]. Use `AbilityButtonViewExtension` to inspect each ability button.

fun BattleUnitInfoView.abilityButtons(): List<AbilityButtonView> = abilityButtons.toList()

/** The remaining steps, displayed as filled white points. */
fun BattleUnitInfoView.remainingSteps(): Int = movementPointsView.filledPoints()

/** The maximum steps per turn: as many white points as displayed, filled or hollow. */
fun BattleUnitInfoView.maximumSteps(): Int = movementPointsView.filledPoints() + movementPointsView.hollowPoints()

/** The remaining casts, displayed as filled golden points. */
fun BattleUnitInfoView.remainingCasts(): Int = castPointsView.filledPoints()

/** The maximum casts per turn: as many golden points as displayed, filled or hollow. */
fun BattleUnitInfoView.maximumCasts(): Int = castPointsView.filledPoints() + castPointsView.hollowPoints()

/** The turns a summon has left before vanishing, as told, or null when none is told (e.g.: it never vanishes). */
fun BattleUnitInfoView.remainingTurnsText(): String? = if (remainingSummonDurationView.visible) remainingSummonDurationView.remainingTurnsText() else null

/** Whether the turns left are told after the name, on its line. */
fun BattleUnitInfoView.isRemainingTurnsAfterName(): Boolean = remainingSummonDurationView.x > nameText.x + nameText.width && remainingSummonDurationView.y == nameText.y - 1

/** The colour of the ring around the portrait, its player's, or null when it has none. */
fun BattleUnitInfoView.portraitRingColor() = unitPortraitView.ringColor()

fun BattleUnitInfoView.movementPointColors() = movementPointsView.pointColors()

fun BattleUnitInfoView.castPointColors() = castPointsView.pointColors()

fun BattleUnitInfoView.selectedAbilityIndexes(): List<Int> = abilityButtons.indices.filter { index -> abilityButtons[index].isSelected() }

/** Whether what the selected ability does is displayed, under the ability buttons. */
fun BattleUnitInfoView.isAbilityInfoDisplayed(): Boolean = visible && abilityInfoView.visible

fun BattleUnitInfoView.displayedAbilityName(): String = abilityInfoView.abilityName()

/** Whether the ability info starts below the bottom of the ability buttons. */
fun BattleUnitInfoView.isAbilityInfoUnderAbilityButtons(): Boolean = abilityInfoView.getGlobalBounds().top >= abilityButtons.maxOf { button -> button.getGlobalBounds().bottom }

/** Whether the inventory button, which opens the inventory, is displayed: never while the header is collapsed. */
fun BattleUnitInfoView.isInventoryButtonDisplayed(): Boolean = header.visible && inventoryButton.visible && inventoryButton.findViewByName(InventoryButtonView.CHEST) != null

/** Whether the header is collapsed to the strip of what a cast spends, as while the info of an ability is displayed. */
fun BattleUnitInfoView.isHeaderCollapsed(): Boolean = isFocused && strip.visible && !header.visible

fun BattleUnitInfoView.isNameDisplayed(): Boolean = header.visible && nameText.visible && nameText.text.isNotEmpty()

fun BattleUnitInfoView.areEffectsDisplayed(): Boolean = header.visible && effectsView.visible

/** Whether the ability buttons and the info have slid to their place under the header, collapsed or not. */
fun BattleUnitInfoView.areAbilityButtonsUnderHeader(): Boolean = body.y == if (isFocused) BattleUnitInfoView.FOCUSED_ABILITIES_Y else BattleUnitInfoView.ABILITIES_Y

suspend fun ViewsForTesting.clickInventoryButton(battleUnitInfoView: BattleUnitInfoView) = clickOn(battleUnitInfoView.inventoryButton)

suspend fun ViewsForTesting.clickAbility(
    battleUnitInfoView: BattleUnitInfoView,
    index: Int,
) = clickAbilityButton(battleUnitInfoView.abilityButtons[index])
