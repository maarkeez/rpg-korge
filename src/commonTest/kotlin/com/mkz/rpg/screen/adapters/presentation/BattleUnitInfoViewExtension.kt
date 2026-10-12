package com.mkz.rpg.screen.adapters.presentation

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

fun BattleUnitInfoView.movementPointColors() = movementPointsView.pointColors()

fun BattleUnitInfoView.castPointColors() = castPointsView.pointColors()

fun BattleUnitInfoView.selectedAbilityIndexes(): List<Int> = abilityButtons.indices.filter { index -> abilityButtons[index].isSelected() }

suspend fun ViewsForTesting.clickAbility(
    battleUnitInfoView: BattleUnitInfoView,
    index: Int,
) = clickAbilityButton(battleUnitInfoView.abilityButtons[index])
