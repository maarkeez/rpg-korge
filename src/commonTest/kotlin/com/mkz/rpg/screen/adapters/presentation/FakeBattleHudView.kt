package com.mkz.rpg.screen.adapters.presentation

import korlibs.math.geom.Size

// Records which part of the HUD the presenters ask to display, instead of drawing it.
class FakeBattleHudView : BattleHudView(Size(0, 0), FakeBattleUnitInfoView(), FakeAttackPreviewView(), FakeInventoryView()) {
    enum class Display { BATTLE_UNIT_INFO, ATTACK_PREVIEW, INVENTORY, HIDDEN }

    val displays = mutableListOf<Display>()

    override fun displayBattleUnitInfoView() {
        displays += Display.BATTLE_UNIT_INFO
    }

    override fun displayAttackPreviewView() {
        displays += Display.ATTACK_PREVIEW
    }

    override fun displayInventoryView() {
        displays += Display.INVENTORY
    }

    override fun hide() {
        displays += Display.HIDDEN
    }
}
