package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class UnitPortraitView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(size) {
    companion object {
        const val PORTRAIT = "PORTRAIT"
    }

    internal val battleUnitPortrait =
        uiButton("").also { button ->
            button.size = size
            button.bgColorOut = Colors.WHITE
            button.bgColorOver = Colors.LIGHTSKYBLUE
            button.background.borderColor = Colors.LIGHTGRAY
        }

    init {
        addChild(battleUnitPortrait)
    }

    fun display(unitId: String) {
        battleUnitPortrait.findViewByName(PORTRAIT)?.removeFromParent()
        resourcesLoader.bitmapOrNull("unit/${unitId}_portrait.png")?.let { avatarBitmap ->
            battleUnitPortrait.image(avatarBitmap) {
                name = PORTRAIT
                smoothing = false
                scale = 3.0
                centerOn(battleUnitPortrait)
            }
        }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
