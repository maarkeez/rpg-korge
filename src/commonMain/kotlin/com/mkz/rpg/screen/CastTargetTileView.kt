package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/** What a valid cast tile contains. The two kinds differ in glyph shape, not only in color. */
enum class CastTargetKind {
    /** An occupied tile: a crosshair glyph. */
    TARGET_UNIT,

    /** A vacant tile: a small diamond glyph. */
    TARGET_TILE,
}

/** A side of a cast tile that touches another tile of the same cast group. No border is drawn there. */
enum class CastEdge { TOP, RIGHT, BOTTOM, LEFT }

/**
 * One valid cast tile, drawn procedurally in art pixels scaled by [PIXEL_SCALE]: a solid 1 art pixel border on
 * the sides not shared with the cast group (so a multi-tile group reads as one connected outline) plus a glyph.
 */
class CastTargetTileView(
    val kind: CastTargetKind,
    val joinedEdges: Set<CastEdge>,
) : Container() {
    companion object {
        const val GLYPH = "CAST_GLYPH"

        private const val TILE_ART_PIXELS = 16
    }

    init {
        drawBorder()
        drawGlyph()
    }

    private fun drawBorder() {
        val last = TILE_ART_PIXELS - 1
        for (i in 0..last) {
            if (CastEdge.TOP !in joinedEdges) addChild(pixel(i, 0, UiPalette.castValid))
            if (CastEdge.BOTTOM !in joinedEdges) addChild(pixel(i, last, UiPalette.castValid))
            if (CastEdge.LEFT !in joinedEdges) addChild(pixel(0, i, UiPalette.castValid))
            if (CastEdge.RIGHT !in joinedEdges) addChild(pixel(last, i, UiPalette.castValid))
        }
    }

    private fun drawGlyph() {
        val glyph = Container().also { it.name = GLYPH }
        addChild(glyph)
        val cells =
            when (kind) {
                // 5x5 plus sign with a hollow center, placed in the top-right corner.
                CastTargetKind.TARGET_UNIT -> listOf(2 to 0, 2 to 1, 0 to 2, 1 to 2, 3 to 2, 4 to 2, 2 to 3, 2 to 4)
                // 3x3 diamond outline.
                CastTargetKind.TARGET_TILE -> listOf(1 to 0, 0 to 1, 2 to 1, 1 to 2)
            }
        val originX = if (kind == CastTargetKind.TARGET_UNIT) 9 else 11
        val originY = 2
        for (y in -1..5) {
            for (x in -1..5) {
                if (kind == CastTargetKind.TARGET_TILE && (x > 3 || y > 3)) continue
                glyph.addChild(pixel(originX + x, originY + y, UiPalette.panel, alpha = 0.6))
            }
        }
        cells.forEach { (x, y) -> glyph.addChild(pixel(originX + x, originY + y, UiPalette.castValid)) }
    }

    private fun pixel(
        x: Int,
        y: Int,
        color: RGBA,
        alpha: Double = 1.0,
    ): SolidRect =
        SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), color).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
            it.alpha = alpha
        }
}

/** Dim overlay for tiles outside every valid cast tile: dark horizontal stripes on every other art pixel row. */
class DimTileView : Container() {
    init {
        for (y in 0 until 16 step 2) {
            addChild(
                SolidRect(Size(16 * PIXEL_SCALE, PIXEL_SCALE), UiPalette.panel).also {
                    it.y = (y * PIXEL_SCALE).toDouble()
                    it.alpha = 0.5
                },
            )
        }
    }
}
