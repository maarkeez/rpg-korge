package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.View

// Page object of [HudView].

/** The views currently displayed by the HUD (e.g.: the battle unit info or the cast preview). */
fun HudView.displayedViews(): List<View> = children.toList()

/** Whether the HUD displays the info of the selected battle unit. */
fun HudView.isDisplayingBattleUnitInfo(): Boolean = children.any { child -> child is BattleUnitInfoView }

/** Whether the HUD displays the panel to give gold. */
fun HudView.isDisplayingGiveGold(): Boolean = children.any { child -> child is GiveGoldView }

/** Whether the HUD displays the inventory. */
fun HudView.isDisplayingInventory(): Boolean = children.any { child -> child is InventoryView }

/** Whether the HUD displays the preview of an ability cast. */
fun HudView.isDisplayingCastPreview(): Boolean = children.any { child -> child is CastPreviewView }
