package com.mkz.rpg.screen.adapters.presentation

import korlibs.math.geom.Size

// Records which part of the HUD the presenters ask to display, instead of drawing it.
class FakeBattleHudView : BattleHudView(Size(0, 0), FakeBattleUnitInfoView(), FakeAttackPreviewView(), FakeInventoryView(), FakeGiveGoldView()) {
    enum class Display { BATTLE_UNIT_INFO, ATTACK_PREVIEW, INVENTORY, GIVE_GOLD, HIDDEN }

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

    override fun displayGiveGoldView() {
        displays += Display.GIVE_GOLD
    }

    override fun hide() {
        displays += Display.HIDDEN
    }
}
