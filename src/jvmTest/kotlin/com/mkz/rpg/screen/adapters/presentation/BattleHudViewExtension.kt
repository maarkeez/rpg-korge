package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.View

// Page object of [BattleHudView].

/** The views currently displayed by the HUD (e.g.: the battle unit info or the attack preview). */
fun BattleHudView.displayedViews(): List<View> = children.toList()
