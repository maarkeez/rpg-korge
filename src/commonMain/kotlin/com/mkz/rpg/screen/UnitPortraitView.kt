package com.mkz.rpg.screen

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class UnitPortraitView(
    size: Size,
) : UIContainer(size) {
    private val battleUnitPortrait =
        uiButton("").also { button ->
            button.size = size
            button.bgColorOut = Colors.WHITE
            button.bgColorOver = Colors.LIGHTSKYBLUE
            button.background.borderColor = Colors.LIGHTGRAY
        }
    private lateinit var knightPortrait: Bitmap
    private lateinit var ratPortrait: Bitmap

    suspend fun loadAssets() {
        knightPortrait = resourcesVfs["unit/knight_portrait.png"].readBitmap()
        ratPortrait = resourcesVfs["unit/rat_portrait.png"].readBitmap()
    }

    init {
        addChild(battleUnitPortrait)
    }

    fun display(unitId: String) {
        battleUnitPortrait.findViewByName("portrait")?.removeFromParent()
        when (unitId) {
            "knight" -> knightPortrait
            "rat" -> ratPortrait
            else -> null
        }?.let { avatarBitmap ->
            battleUnitPortrait.image(avatarBitmap) {
                name = "portrait"
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
