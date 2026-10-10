package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class UnitPortraitView(
    size: Size,
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : UIContainer(size) {
    private val battleUnitPortrait =
        uiButton("").also { button ->
            button.size = size
            button.bgColorOut = UiPalette.portraitBackground
            button.bgColorOver = UiPalette.portraitBackgroundOver
            button.background.borderColor = UiPalette.panelBorder
        }

    suspend fun loadAssets() {
        sprites.load()
    }

    init {
        addChild(battleUnitPortrait)
    }

    fun display(unitId: String) {
        battleUnitPortrait.findViewByName("portrait")?.removeFromParent()
        battleUnitPortrait.image(sprites.portrait(unitId)) {
            name = "portrait"
            smoothing = false
            scale = PIXEL_SCALE.toDouble()
            centerOn(battleUnitPortrait)
        }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
