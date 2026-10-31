package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter
import kotlin.math.abs

// Page object of [AbilityButtonView].

fun AbilityButtonView.isAbilityIconDisplayed(): Boolean = findViewByName(AbilityButtonView.ABILITY) != null

private fun AbilityButtonView.abilityIconMatrix() = (findViewByName(AbilityButtonView.ABILITY)?.filter as? ColorMatrixFilter)?.colorMatrix

/** Whether the icon is darkened, as while the ability is in cooldown. */
fun AbilityButtonView.isAbilityIconDarkened(): Boolean = abilityIconMatrix() == AbilityButtonView.DARK_MATRIX

/** Whether the icon is tinted blue, as while there is not enough mana to cast the ability. */
fun AbilityButtonView.isAbilityIconTintedBlue(): Boolean = abilityIconMatrix() == AbilityButtonView.BLUE_MATRIX

/** The cooldown turns left told over the icon, or null when none is told. */
fun AbilityButtonView.cooldownBadgeText(): String? =
    (findViewByName(AbilityButtonView.COOLDOWN) as? Container)
        ?.children
        ?.filterIsInstance<Text>()
        ?.firstOrNull()
        ?.text

fun AbilityButtonView.cooldownBadgeCount(): Int = children.count { child -> child.name == AbilityButtonView.COOLDOWN }

/** Whether the cooldown turns left are told in the middle of the button. */
fun AbilityButtonView.isCooldownCentered(): Boolean {
    val turnsLeft = (findViewByName(AbilityButtonView.COOLDOWN) as? Container)?.children?.filterIsInstance<Text>()?.firstOrNull() ?: return false
    val bounds = turnsLeft.getBounds(this)
    return abs(bounds.centerX - width / 2) < 1 && abs(bounds.centerY - height / 2) < 1
}

/** The mana cost told by the badge, or null when no badge is displayed. */
fun AbilityButtonView.costBadgeText(): String? = (findViewByName(AbilityButtonView.COST) as? PillView)?.labelText()

/** Whether the ability is ringed in gold, as when it is selected. */
fun AbilityButtonView.isSelected(): Boolean = selectionIconCount() > 0

fun AbilityButtonView.selectionIconCount(): Int = children.count { child -> child.name == AbilityButtonView.ABILITY_SELECTION }

suspend fun ViewsForTesting.clickAbilityButton(abilityButtonView: AbilityButtonView) = clickOn(abilityButtonView)
