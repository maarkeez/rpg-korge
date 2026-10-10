package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/** What a unit overlay shows. Built from the current battle unit state. */
data class UnitOverlayState(
    val remainingHealthPoints: Int,
    val maximumHealthPoints: Int,
    val isEnemy: Boolean,
    val onTurnStartedEffectCount: Int,
    val onDefeatedEffectCount: Int,
    /** What the previewed cast would do to this unit. `null` when the unit isn't a preview target. */
    val preview: UnitPreviewState? = null,
) {
    val hpFraction: Double
        get() = if (maximumHealthPoints <= 0) 0.0 else (remainingHealthPoints.toDouble() / maximumHealthPoints).coerceIn(0.0, 1.0)

    val statusCount: Int get() = onTurnStartedEffectCount + onDefeatedEffectCount
}

/** Predicted outcome for one unit while a cast is previewed. */
data class UnitPreviewState(
    val hpAfter: Int,
    val isLethal: Boolean,
    val pendingOnTurnCount: Int,
    val pendingOnDefeatCount: Int,
)

/**
 * HP bar and status pips drawn over one unit tile, in art pixels scaled by [PIXEL_SCALE].
 * Everything stays inside the top and bottom 3 art pixels of the tile so terrain hazards remain visible.
 *
 * - The bar fill takes the ally or enemy edge color on a dark track, so the side is visible at full health.
 * - Allies get a flat bar, enemies a notched bar (the first top pixel and the last bottom pixel are cut), so the
 *   difference doesn't rely on color.
 * - Status pips: a filled square is an on-turn effect, a hollow ring is an on-death effect.
 *   At most [MAX_VISIBLE_PIPS] are drawn, then a "+".
 * - Preview: the predicted HP change is a checkered ghost segment on the bar (red for loss, green for gain),
 *   predicted lethal hits get a skull, and statuses to be applied are pips preceded by a "+" badge.
 */
class UnitOverlayView : Container() {
    companion object {
        const val HP_BAR_TRACK = "HP_BAR_TRACK"
        const val HP_BAR_FILL = "HP_BAR_FILL"
        const val STATUS_PIP = "STATUS_PIP"
        const val STATUS_OVERFLOW = "STATUS_OVERFLOW"
        const val HP_BAR_GHOST = "HP_BAR_GHOST"
        const val SKULL_GLYPH = "SKULL_GLYPH"
        const val PENDING_BADGE = "PENDING_BADGE"
        const val PENDING_PIP = "PENDING_PIP"

        const val HP_BAR_WIDTH_ART_PIXELS = 14
        const val HP_BAR_HEIGHT_ART_PIXELS = 2
        const val MAX_VISIBLE_PIPS = 2
        private const val TILE_ART_PIXELS = 16
        private const val HP_BAR_TOP_ART_PIXEL = 13
        private const val PIP_SIZE_ART_PIXELS = 3
        private const val PENDING_ROW_ART_PIXEL = 5
        private const val PLUS_WIDTH = 3
        private const val SKULL_X = 10
        private const val SKULL_Y = 4
        private val SKULL = listOf("#####", "# # #", "#####", " ### ", " # # ")
        private val PLUS = listOf(" # ", "###", " # ")
    }

    var state: UnitOverlayState? = null
        private set

    val hpFraction: Double get() = state?.hpFraction ?: 0.0

    val visiblePipCount: Int get() = children.count { it.name == STATUS_PIP }

    val hasOverflow: Boolean get() = children.any { it.name == STATUS_OVERFLOW }

    val ghostPixelCount: Int get() = children.count { it.name == HP_BAR_GHOST }

    val hasSkull: Boolean get() = children.any { it.name == SKULL_GLYPH }

    val pendingPipCount: Int get() = children.count { it.name == PENDING_PIP }

    val hasPendingBadge: Boolean get() = children.any { it.name == PENDING_BADGE }

    fun display(state: UnitOverlayState) {
        this.state = state
        removeChildren()
        drawHpBar(state)
        drawStatusPips(state)
        state.preview?.let { drawPreview(it) }
    }

    private fun drawHpBar(state: UnitOverlayState) {
        val edge = if (state.isEnemy) UiPalette.enemy else UiPalette.ally
        val left = (TILE_ART_PIXELS - HP_BAR_WIDTH_ART_PIXELS) / 2
        val filledPixels = filledPixels(state)
        val previewPixels = state.preview?.let { pixelsFor(it.hpAfter, state.maximumHealthPoints) }
        val solidPixels = if (previewPixels == null) filledPixels else minOf(filledPixels, previewPixels)
        val ghostColor = if (previewPixels != null && previewPixels > filledPixels) UiPalette.hpGain else UiPalette.hpLoss
        for (row in 0 until HP_BAR_HEIGHT_ART_PIXELS) {
            // Enemies are notched: top row starts one pixel in, bottom row ends one pixel early.
            val start = if (state.isEnemy && row == 0) 1 else 0
            val end = if (state.isEnemy && row == HP_BAR_HEIGHT_ART_PIXELS - 1) HP_BAR_WIDTH_ART_PIXELS - 1 else HP_BAR_WIDTH_ART_PIXELS
            val y = HP_BAR_TOP_ART_PIXEL + row
            artRect(left + start, y, end - start, 1, UiPalette.panel, HP_BAR_TRACK)
            val fillEnd = minOf(end, solidPixels)
            if (fillEnd > start) artRect(left + start, y, fillEnd - start, 1, edge, HP_BAR_FILL)
            if (previewPixels != null) {
                // Checkered, so the predicted change doesn't rely on color.
                for (x in maxOf(start, solidPixels) until minOf(end, maxOf(filledPixels, previewPixels))) {
                    if ((x + row) % 2 == 0) artRect(left + x, y, 1, 1, ghostColor, HP_BAR_GHOST)
                }
            }
        }
    }

    /** A living unit always shows at least one pixel, so a nearly dead unit is not mistaken for an empty bar. */
    private fun filledPixels(state: UnitOverlayState): Int = pixelsFor(state.remainingHealthPoints, state.maximumHealthPoints)

    private fun pixelsFor(
        healthPoints: Int,
        maximumHealthPoints: Int,
    ): Int {
        if (healthPoints <= 0 || maximumHealthPoints <= 0) return 0
        val fraction = (healthPoints.toDouble() / maximumHealthPoints).coerceIn(0.0, 1.0)
        return (fraction * HP_BAR_WIDTH_ART_PIXELS).toInt().coerceIn(1, HP_BAR_WIDTH_ART_PIXELS)
    }

    private fun drawPreview(preview: UnitPreviewState) {
        if (preview.isLethal) {
            addChild(PixelGlyphs.glyph(SKULL, UiPalette.panel, PIXEL_SCALE).also { positionGlyph(it, SKULL_X + 1, SKULL_Y + 1) })
            addChild(PixelGlyphs.glyph(SKULL, UiPalette.textPrimary, PIXEL_SCALE, SKULL_GLYPH).also { positionGlyph(it, SKULL_X, SKULL_Y) })
        }
        val pending = List(preview.pendingOnTurnCount) { PipKind.ON_TURN } + List(preview.pendingOnDefeatCount) { PipKind.ON_DEFEAT }
        if (pending.isEmpty()) return
        addChild(PixelGlyphs.glyph(PLUS, UiPalette.textPrimary, PIXEL_SCALE, PENDING_BADGE).also { positionGlyph(it, 0, PENDING_ROW_ART_PIXEL) })
        pending.take(MAX_VISIBLE_PIPS).forEachIndexed { index, kind ->
            val pip = pipView(kind, x = PLUS_WIDTH + 1 + index * (PIP_SIZE_ART_PIXELS + 1), y = PENDING_ROW_ART_PIXEL)
            pip.name = PENDING_PIP
            addChild(pip)
        }
    }

    private fun positionGlyph(
        glyph: Container,
        x: Int,
        y: Int,
    ) {
        glyph.x = (x * PIXEL_SCALE).toDouble()
        glyph.y = (y * PIXEL_SCALE).toDouble()
    }

    private fun drawStatusPips(state: UnitOverlayState) {
        val pips = List(state.onTurnStartedEffectCount) { PipKind.ON_TURN } + List(state.onDefeatedEffectCount) { PipKind.ON_DEFEAT }
        val visible = pips.take(MAX_VISIBLE_PIPS)
        val step = PIP_SIZE_ART_PIXELS + 1
        visible.forEachIndexed { index, kind -> drawPip(kind, x = index * step) }
        if (pips.size > MAX_VISIBLE_PIPS) drawOverflow(x = visible.size * step)
    }

    private fun drawPip(
        kind: PipKind,
        x: Int,
    ) {
        addChild(pipView(kind, x, y = 0).also { it.name = STATUS_PIP })
    }

    /** A filled square is an on-turn status, a hollow 3x3 ring is an on-death status. */
    private fun pipView(
        kind: PipKind,
        x: Int,
        y: Int,
    ): Container =
        Container().also { container ->
            when (kind) {
                PipKind.ON_TURN -> container.addChild(artRectView(x, y, PIP_SIZE_ART_PIXELS, PIP_SIZE_ART_PIXELS, UiPalette.conditional))
                PipKind.ON_DEFEAT ->
                    listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2).forEach { (dx, dy) ->
                        container.addChild(artPixel(x + dx, y + dy, UiPalette.danger))
                    }
            }
        }

    private fun drawOverflow(x: Int) {
        val container = Container().also { it.name = STATUS_OVERFLOW }
        addChild(container)
        container.addChild(artRectView(x + 1, 0, 1, 3, UiPalette.textPrimary))
        container.addChild(artRectView(x, 1, 3, 1, UiPalette.textPrimary))
    }

    private fun artPixel(
        x: Int,
        y: Int,
        color: RGBA,
    ) = artRectView(x, y, 1, 1, color)

    private fun artRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: RGBA,
        viewName: String,
    ) {
        solidRect(Size(width * PIXEL_SCALE, height * PIXEL_SCALE), color) {
            name = viewName
            this.x = (x * PIXEL_SCALE).toDouble()
            this.y = (y * PIXEL_SCALE).toDouble()
        }
    }

    private fun artRectView(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: RGBA,
    ): SolidRect =
        SolidRect(Size(width * PIXEL_SCALE, height * PIXEL_SCALE), color).also {
            it.x = (x * PIXEL_SCALE).toDouble()
            it.y = (y * PIXEL_SCALE).toDouble()
        }

    private enum class PipKind { ON_TURN, ON_DEFEAT }
}
