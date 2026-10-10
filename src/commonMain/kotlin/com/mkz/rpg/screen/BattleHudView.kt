package com.mkz.rpg.screen

import korlibs.korge.ui.UIContainer
import korlibs.korge.view.View
import korlibs.math.geom.Size

/**
 * Contextual bottom sheet. [panel] is the opaque backdrop that makes the sheet consume touches; it is
 * shown only while the sheet has content, so taps in an idle sheet area reach the map.
 */
class BattleHudView(
    size: Size,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val attackPreviewView: AttackPreviewView,
    private val panel: View? = null,
) : UIContainer(size) {
    init {
        panel?.mouseEnabled = true
        panel?.visible = false
    }

    fun displayBattleUnitInfoView() {
        removeChildren()
        addChild(battleUnitInfoView)
        panel?.visible = true
    }

    fun displayAttackPreviewView() {
        removeChildren()
        addChild(attackPreviewView)
        panel?.visible = true
    }

    fun hide() {
        removeChildren()
        panel?.visible = false
    }
}
