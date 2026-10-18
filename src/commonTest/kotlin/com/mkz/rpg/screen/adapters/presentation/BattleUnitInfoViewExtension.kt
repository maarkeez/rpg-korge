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
fun BattleUnitInfoView.remainingTurnsText(): String? = if (remainingTurnsView.visible) remainingTurnsView.remainingTurnsText() else null

/** Whether the turns left are told after the remaining casts, on their line. */
fun BattleUnitInfoView.isRemainingTurnsAfterCasts(): Boolean = remainingTurnsView.x > castPointsView.x + castPointsView.width && remainingTurnsView.y == castPointsView.y

fun BattleUnitInfoView.movementPointColors() = movementPointsView.pointColors()

fun BattleUnitInfoView.castPointColors() = castPointsView.pointColors()

fun BattleUnitInfoView.selectedAbilityIndexes(): List<Int> = abilityButtons.indices.filter { index -> abilityButtons[index].isSelected() }

/** Whether the bag, which opens the inventory, is displayed. */
fun BattleUnitInfoView.isBagDisplayed(): Boolean = bagButton.visible && bagButton.findViewByName(BagButtonView.BAG) != null

suspend fun ViewsForTesting.clickBag(battleUnitInfoView: BattleUnitInfoView) = clickOn(battleUnitInfoView.bagButton)

suspend fun ViewsForTesting.clickAbility(
    battleUnitInfoView: BattleUnitInfoView,
    index: Int,
) = clickAbilityButton(battleUnitInfoView.abilityButtons[index])
