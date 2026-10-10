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
) {
    val hpFraction: Double
        get() = if (maximumHealthPoints <= 0) 0.0 else (remainingHealthPoints.toDouble() / maximumHealthPoints).coerceIn(0.0, 1.0)

    val statusCount: Int get() = onTurnStartedEffectCount + onDefeatedEffectCount
}

/**
 * HP bar and status pips drawn over one unit tile, in art pixels scaled by [PIXEL_SCALE].
 * Everything stays inside the top and bottom 3 art pixels of the tile so terrain hazards remain visible.
 *
 * - The bar fill takes the ally or enemy edge color on a dark track, so the side is visible at full health.
 * - Allies get a flat bar, enemies a notched bar (the first top pixel and the last bottom pixel are cut), so the
 *   difference doesn't rely on color.
 * - Status pips: a filled square is an on-turn effect, a hollow ring is an on-death effect.
 *   At most [MAX_VISIBLE_PIPS] are drawn, then a "+".
 */
class UnitOverlayView : Container() {
    companion object {
        const val HP_BAR_TRACK = "HP_BAR_TRACK"
        const val HP_BAR_FILL = "HP_BAR_FILL"
        const val STATUS_PIP = "STATUS_PIP"
        const val STATUS_OVERFLOW = "STATUS_OVERFLOW"

        const val HP_BAR_WIDTH_ART_PIXELS = 14
        const val HP_BAR_HEIGHT_ART_PIXELS = 2
        const val MAX_VISIBLE_PIPS = 2
        private const val TILE_ART_PIXELS = 16
        private const val HP_BAR_TOP_ART_PIXEL = 13
        private const val PIP_SIZE_ART_PIXELS = 3
    }

    var state: UnitOverlayState? = null
        private set

    val hpFraction: Double get() = state?.hpFraction ?: 0.0

    val visiblePipCount: Int get() = children.count { it.name == STATUS_PIP }

    val hasOverflow: Boolean get() = children.any { it.name == STATUS_OVERFLOW }

    fun display(state: UnitOverlayState) {
        this.state = state
        removeChildren()
        drawHpBar(state)
        drawStatusPips(state)
    }

    private fun drawHpBar(state: UnitOverlayState) {
        val edge = if (state.isEnemy) UiPalette.enemy else UiPalette.ally
        val left = (TILE_ART_PIXELS - HP_BAR_WIDTH_ART_PIXELS) / 2
        val filledPixels = filledPixels(state)
        for (row in 0 until HP_BAR_HEIGHT_ART_PIXELS) {
            // Enemies are notched: top row starts one pixel in, bottom row ends one pixel early.
            val start = if (state.isEnemy && row == 0) 1 else 0
            val end = if (state.isEnemy && row == HP_BAR_HEIGHT_ART_PIXELS - 1) HP_BAR_WIDTH_ART_PIXELS - 1 else HP_BAR_WIDTH_ART_PIXELS
            val y = HP_BAR_TOP_ART_PIXEL + row
            artRect(left + start, y, end - start, 1, UiPalette.panel, HP_BAR_TRACK)
            val fillEnd = minOf(end, filledPixels)
            if (fillEnd > start) artRect(left + start, y, fillEnd - start, 1, edge, HP_BAR_FILL)
        }
    }

    /** A living unit always shows at least one pixel, so a nearly dead unit is not mistaken for an empty bar. */
    private fun filledPixels(state: UnitOverlayState): Int {
        if (state.remainingHealthPoints <= 0) return 0
        return (state.hpFraction * HP_BAR_WIDTH_ART_PIXELS).toInt().coerceIn(1, HP_BAR_WIDTH_ART_PIXELS)
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
        when (kind) {
            PipKind.ON_TURN -> artRect(x, 0, PIP_SIZE_ART_PIXELS, PIP_SIZE_ART_PIXELS, UiPalette.conditional, STATUS_PIP)
            PipKind.ON_DEFEAT -> {
                // Hollow ring: 3x3 with an empty center.
                val container = Container().also { it.name = STATUS_PIP }
                addChild(container)
                listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2).forEach { (dx, dy) ->
                    container.addChild(artPixel(x + dx, dy, UiPalette.danger))
                }
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
