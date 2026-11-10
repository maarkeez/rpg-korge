package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.View

fun HudView.displayedViews(): List<View> = children.toList()

fun HudView.isDisplayingBattleUnitInfo(): Boolean = children.any { child -> child is BattleUnitInfoView }

fun HudView.isDisplayingGiveGold(): Boolean = children.any { child -> child is GiveGoldView }

fun HudView.isDisplayingInventory(): Boolean = children.any { child -> child is InventoryView }

fun HudView.isDisplayingCastPreview(): Boolean = children.any { child -> child is CastPreviewView }
