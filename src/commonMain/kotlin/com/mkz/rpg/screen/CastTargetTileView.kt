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
    /** The cast would defeat the unit on this tile right now. Adds a skull glyph. */
    val lethal: Boolean = false,
) : Container() {
    companion object {
        const val GLYPH = "CAST_GLYPH"
        const val SKULL_HINT = "CAST_SKULL"

        private const val TILE_ART_PIXELS = 16
        private const val SKULL_X = 1
        private const val SKULL_Y = 5
    }

    init {
        drawBorder()
        drawGlyph()
        if (lethal) drawSkull()
    }

    private fun drawSkull() {
        addChild(
            PixelGlyphs.glyph(PixelGlyphs.SKULL, UiPalette.panel, PIXEL_SCALE).also {
                it.x = (SKULL_X + 1) * PIXEL_SCALE.toDouble()
                it.y = (SKULL_Y + 1) * PIXEL_SCALE.toDouble()
            },
        )
        addChild(
            PixelGlyphs.glyph(PixelGlyphs.SKULL, UiPalette.textPrimary, PIXEL_SCALE, SKULL_HINT).also {
                it.x = SKULL_X * PIXEL_SCALE.toDouble()
                it.y = SKULL_Y * PIXEL_SCALE.toDouble()
            },
        )
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

/**
 * A neighbour that would receive a spread status if the previewed target is defeated: a dashed 1 art pixel outline.
 * [strong] (3 on, 1 off) means the cast defeats the target, so the spread happens. Faint (2 on, 2 off, half transparent)
 * means it only spreads if the target is defeated later. Solid borders stay reserved for certain, valid casts.
 */
class ConditionalTileView(
    val strong: Boolean,
) : Container() {
    init {
        val last = TILE_ART_PIXELS - 1
        for (i in 0..last) {
            if (i % DASH_PERIOD >= (if (strong) 3 else 2)) continue
            listOf(i to 0, i to last, 0 to i, last to i).forEach { (x, y) -> addChild(pixel(x, y)) }
        }
    }

    private fun pixel(
        x: Int,
        y: Int,
    ): SolidRect =
        SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), UiPalette.conditional).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
            it.alpha = if (strong) 1.0 else 0.6
        }

    private companion object {
        const val TILE_ART_PIXELS = 16
        const val DASH_PERIOD = 4
    }
}

/**
 * Half of the connector between a defeated target and a neighbour: a dashed line from the tile centre to the
 * [edge] that faces the other tile. The neighbour's half also ends in an arrowhead at the centre, so the spread has a direction.
 */
class SpreadConnectorView(
    val edge: CastEdge,
    val arrowhead: Boolean,
    val strong: Boolean,
) : Container() {
    init {
        for (u in 0 until LINE_LENGTH) {
            if (u % DASH_PERIOD >= (if (strong) 3 else 1)) continue
            addChild(pixel(u, 0))
        }
        if (arrowhead) {
            for ((u, v) in listOf(0 to 0, 1 to -1, 1 to 1, 2 to -2, 2 to 2)) addChild(pixel(u, v))
        }
    }

    /** [u] runs from the tile centre towards [edge], [v] runs across the line. */
    private fun pixel(
        u: Int,
        v: Int,
    ): SolidRect {
        val (x, y) =
            when (edge) {
                CastEdge.TOP -> CENTER + v to CENTER - u
                CastEdge.BOTTOM -> CENTER + v to CENTER + u
                CastEdge.LEFT -> CENTER - u to CENTER + v
                CastEdge.RIGHT -> CENTER + u to CENTER + v
            }
        return SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), UiPalette.conditional).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
        }
    }

    private companion object {
        const val CENTER = 8
        const val LINE_LENGTH = 8
        const val DASH_PERIOD = 4
    }
}
