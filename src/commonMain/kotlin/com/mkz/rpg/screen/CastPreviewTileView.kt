package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/**
 * A tile affected by the previewed cast: a reticle made of four corner brackets and four ticks at the middle of
 * each side. The shape differs from the solid valid-target border, so the previewed group is never told apart by color alone.
 */
class CastPreviewTileView : Container() {
    init {
        val last = TILE_ART_PIXELS - 1
        for ((cornerX, cornerY) in listOf(0 to 0, last to 0, 0 to last, last to last)) {
            val dx = if (cornerX == 0) 1 else -1
            val dy = if (cornerY == 0) 1 else -1
            for (i in 0 until ARM_LENGTH) {
                addPixel(cornerX + dx * i, cornerY)
                if (i > 0) addPixel(cornerX, cornerY + dy * i)
            }
        }
        for (i in 0 until TICK_LENGTH) {
            addPixel(MID, i)
            addPixel(MID, last - i)
            addPixel(i, MID)
            addPixel(last - i, MID)
        }
    }

    private fun addPixel(
        x: Int,
        y: Int,
    ) {
        addChild(pixel(x, y, UiPalette.castPreview))
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
        const val TICK_LENGTH = 2
        const val MID = 8
    }
}
