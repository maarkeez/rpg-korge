package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.math.geom.Size

open class BattleHudView(
    size: Size,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val attackPreviewView: AttackPreviewView,
    private val inventoryView: InventoryView,
) : UIContainer(size) {
    open fun displayBattleUnitInfoView() {
        removeChildren()
        addChild(battleUnitInfoView)
    }

    open fun displayAttackPreviewView() {
        removeChildren()
        addChild(attackPreviewView)
    }

    open fun displayInventoryView() {
        removeChildren()
        addChild(inventoryView)
    }

    open fun hide() {
        removeChildren()
    }
}
