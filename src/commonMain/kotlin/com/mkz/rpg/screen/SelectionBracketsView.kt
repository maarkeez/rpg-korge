package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/** Four corner brackets around a tile. Enemy brackets are red and add an eye-shaped inspect glyph. */
class SelectionBracketsView(
    isEnemy: Boolean,
) : Container() {
    init {
        val color = if (isEnemy) UiPalette.enemy else UiPalette.selection
        val last = TILE_ART_PIXELS - 1
        for ((cornerX, cornerY) in listOf(0 to 0, last to 0, 0 to last, last to last)) {
            val dx = if (cornerX == 0) 1 else -1
            val dy = if (cornerY == 0) 1 else -1
            for (i in 0 until ARM_LENGTH) {
                addPixel(cornerX + dx * i, cornerY, color)
                if (i > 0) addPixel(cornerX, cornerY + dy * i, color)
            }
        }
        if (isEnemy) addInspectGlyph()
    }

    private fun addInspectGlyph() {
        val glyph = Container().also { it.name = BattlefieldView.INSPECT_GLYPH }
        addChild(glyph)
        // 5x3 eye outline with a pupil, placed inside the top-right bracket.
        val originX = 9
        val originY = 2
        val outline = listOf(1 to 0, 2 to 0, 3 to 0, 0 to 1, 4 to 1, 1 to 2, 2 to 2, 3 to 2)
        outline.forEach { (x, y) -> glyph.addChild(pixel(originX + x, originY + y, UiPalette.textPrimary)) }
        glyph.addChild(pixel(originX + 2, originY + 1, UiPalette.textPrimary))
    }

    private fun addPixel(
        x: Int,
        y: Int,
        color: RGBA,
    ) {
        addChild(pixel(x, y, color))
    }

    private fun pixel(
        x: Int,
        y: Int,
        color: RGBA,
    ): SolidRect =
        SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), color).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
        }

    private companion object {
        const val TILE_ART_PIXELS = 16
        const val ARM_LENGTH = 4
    }
}
