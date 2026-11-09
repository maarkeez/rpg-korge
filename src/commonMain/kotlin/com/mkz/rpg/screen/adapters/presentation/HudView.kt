package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.math.geom.Size

open class HudView(
    size: Size,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val castPreviewView: CastPreviewView,
    private val inventoryView: InventoryView,
    private val giveGoldView: GiveGoldView,
) : UIContainer(size) {
    open fun displayBattleUnitInfoView() {
        removeChildren()
        addChild(battleUnitInfoView)
    }

    open fun displayCastPreviewView() {
        removeChildren()
        addChild(castPreviewView)
    }

    open fun displayInventoryView() {
        removeChildren()
        addChild(inventoryView)
    }

    open fun displayGiveGoldView() {
        removeChildren()
        addChild(giveGoldView)
        giveGoldView.x = GiveGoldView.X
    }

    open fun hide() {
        removeChildren()
    }
}
