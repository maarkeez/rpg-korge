package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting

// Page object of [BattleUnitInfoView]. Use `AbilityButtonViewExtension` to inspect each ability button.

fun BattleUnitInfoView.abilityButtons(): List<AbilityButtonView> = abilityButtons.toList()

fun BattleUnitInfoView.selectedAbilityIndexes(): List<Int> = abilityButtons.indices.filter { index -> abilityButtons[index].isSelected() }

suspend fun ViewsForTesting.clickAbility(
    battleUnitInfoView: BattleUnitInfoView,
    index: Int,
) = clickAbilityButton(battleUnitInfoView.abilityButtons[index])
