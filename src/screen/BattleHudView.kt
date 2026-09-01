package screen

import korlibs.korge.ui.*
import korlibs.math.geom.*

class BattleHudView(
    size: Size,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val attackPreviewView: AttackPreviewView,
): UIContainer(size) {

    fun displayBattleUnitInfoView() {
        removeChildren()
        addChild(battleUnitInfoView)
    }

    fun displayAttackPreviewView() {
        removeChildren()
        addChild(attackPreviewView)
    }

    fun hide() {
        removeChildren()
    }
}
