package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter

// Page object of [AbilityButtonView].

fun AbilityButtonView.isAbilityIconDisplayed(): Boolean = findViewByName(AbilityButtonView.ABILITY) != null

private fun AbilityButtonView.abilityIconMatrix() = (findViewByName(AbilityButtonView.ABILITY)?.filter as? ColorMatrixFilter)?.colorMatrix

/** Whether the icon is darkened, as while the ability is in cooldown. */
fun AbilityButtonView.isAbilityIconDarkened(): Boolean = abilityIconMatrix() == AbilityButtonView.DARK_MATRIX

/** Whether the icon is tinted blue, as while there is not enough mana to cast the ability. */
fun AbilityButtonView.isAbilityIconTintedBlue(): Boolean = abilityIconMatrix() == AbilityButtonView.BLUE_MATRIX

/** The cooldown turns left told by the corner badge, or null when no badge is displayed. */
fun AbilityButtonView.cooldownBadgeText(): String? =
    (findViewByName(AbilityButtonView.COOLDOWN) as? Container)
        ?.children
        ?.filterIsInstance<Text>()
        ?.firstOrNull()
        ?.text

fun AbilityButtonView.cooldownBadgeCount(): Int = children.count { child -> child.name == AbilityButtonView.COOLDOWN }

fun AbilityButtonView.isSelected(): Boolean = selectionIconCount() > 0

fun AbilityButtonView.selectionIconCount(): Int = children.count { child -> child.name == AbilityButtonView.ABILITY_SELECTION }

suspend fun ViewsForTesting.clickAbilityButton(abilityButtonView: AbilityButtonView) = clickOn(abilityButtonView)
