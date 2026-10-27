package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

// Portrait scale: how many screen pixels each pixel of the 32x32 portrait takes, a whole number so it stays sharp.
class UnitPortraitView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val portraitScale: Double = 3.0,
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
                scale = portraitScale
                centerOn(battleUnitPortrait)
            }
        }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
