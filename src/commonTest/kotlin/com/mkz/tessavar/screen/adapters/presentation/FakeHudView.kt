package com.mkz.tessavar.screen.adapters.presentation

import korlibs.math.geom.Size

class FakeHudView : HudView(Size(0, 0), FakeBattleUnitInfoView(), FakeCastPreviewView(), FakeInventoryView(), FakeGiveGoldView()) {
    enum class Display { BATTLE_UNIT_INFO, ATTACK_PREVIEW, INVENTORY, GIVE_GOLD, HIDDEN }

    val displays = mutableListOf<Display>()

    override fun displayBattleUnitInfoView() {
        displays += Display.BATTLE_UNIT_INFO
    }

    override fun displayCastPreviewView() {
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
