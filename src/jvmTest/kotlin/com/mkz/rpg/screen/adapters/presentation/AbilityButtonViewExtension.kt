package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.filter.filter

// Page object of [AbilityButtonView].

fun AbilityButtonView.isAbilityIconDisplayed(): Boolean = findViewByName(AbilityButtonView.ABILITY) != null

fun AbilityButtonView.isAbilityIconDarkened(): Boolean = findViewByName(AbilityButtonView.ABILITY)?.filter != null

fun AbilityButtonView.isSelected(): Boolean = selectionIconCount() > 0

fun AbilityButtonView.selectionIconCount(): Int = children.count { child -> child.name == AbilityButtonView.ABILITY_SELECTION }

suspend fun ViewsForTesting.clickAbilityButton(abilityButtonView: AbilityButtonView) = clickOn(abilityButtonView)
