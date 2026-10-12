package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.View

// Page object of [BattleHudView].

/** The views currently displayed by the HUD (e.g.: the battle unit info or the attack preview). */
fun BattleHudView.displayedViews(): List<View> = children.toList()

/** Whether the HUD displays the info of the selected battle unit. */
fun BattleHudView.isDisplayingBattleUnitInfo(): Boolean = children.any { child -> child is BattleUnitInfoView }

/** Whether the HUD displays the preview of an ability cast. */
fun BattleHudView.isDisplayingAttackPreview(): Boolean = children.any { child -> child is AttackPreviewView }
