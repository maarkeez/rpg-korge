package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/** How a reachable tile is drawn. The two styles differ in shape, not only in color. */
enum class MovementStyle {
    /** The selected unit is an ally that can move: dotted fill. */
    ALLY,

    /** The selected unit is inspected and can't be commanded: hollow outline. */
    INSPECT,
}

/**
 * One reachable tile, drawn procedurally in art pixels scaled by [PIXEL_SCALE].
 * - [MovementStyle.ALLY]: a dot every 4 art pixels.
 * - [MovementStyle.INSPECT]: a 1 art pixel outline, one pixel inside the tile edge.
 * - [hazard]: a small "!" glyph in the top-left corner.
 */
class MovementRangeTileView(
    val style: MovementStyle,
    val hazard: Boolean,
) : Container() {
    companion object {
        const val HAZARD_GLYPH = "HAZARD_GLYPH"

        private const val TILE_ART_PIXELS = 16
        private const val DOT_SPACING = 4
    }

    init {
        when (style) {
            MovementStyle.ALLY -> drawDots()
            MovementStyle.INSPECT -> drawOutline()
        }
        if (hazard) drawHazardGlyph()
    }

    private fun drawDots() {
        for (y in 1 until TILE_ART_PIXELS step DOT_SPACING) {
            for (x in 1 until TILE_ART_PIXELS step DOT_SPACING) {
                addChild(pixel(x, y, UiPalette.move))
            }
        }
    }

    private fun drawOutline() {
        val first = 1
        val last = TILE_ART_PIXELS - 2
        for (i in first..last) {
            addChild(pixel(i, first, UiPalette.enemy))
            addChild(pixel(i, last, UiPalette.enemy))
            if (i != first && i != last) {
                addChild(pixel(first, i, UiPalette.enemy))
                addChild(pixel(last, i, UiPalette.enemy))
            }
        }
    }

    private fun drawHazardGlyph() {
        val glyph = Container().also { it.name = HAZARD_GLYPH }
        addChild(glyph)
        // 3x5 exclamation mark on a dark backing so it reads on any terrain.
        val originX = 2
        val originY = 2
        for (y in 0 until 5) for (x in 0 until 3) glyph.addChild(pixel(originX + x, originY + y, UiPalette.panel))
        for (y in 0 until 3) glyph.addChild(pixel(originX + 1, originY + y, UiPalette.selection))
        glyph.addChild(pixel(originX + 1, originY + 4, UiPalette.selection))
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
}
