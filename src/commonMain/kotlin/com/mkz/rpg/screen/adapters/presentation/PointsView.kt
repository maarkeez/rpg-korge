package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.roundRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * Displays an amount out of a maximum without text: one rounded pip per point, filled while it remains and tinted with
 * an outline once it has been spent, e.g.: ▬▬▭ for 2 out of 3.
 */
class PointsView(
    private val color: RGBA,
) : Container() {
    companion object {
        const val FILLED = "FILLED"
        const val HOLLOW = "HOLLOW"

        const val POINT_WIDTH = 14.0
        const val POINT_HEIGHT = 7.0
        const val POINT_SPACING = 3.0
        const val SPENT_POINT_ALPHA = 0.14
        private const val SPENT_POINT_STROKE_THICKNESS = 1.5
    }

    internal val points: List<RoundRect>
        get() = children.filterIsInstance<RoundRect>()

    fun display(
        remaining: Int,
        maximum: Int,
    ) {
        removeChildren()
        repeat(maximum) { index ->
            val isFilled = index < remaining
            // The outline is drawn half outside the pip, so spent pips are smaller to look as big as filled ones.
            val inset = if (isFilled) 0.0 else SPENT_POINT_STROKE_THICKNESS / 2
            roundRect(
                Size(POINT_WIDTH - inset * 2, POINT_HEIGHT - inset * 2),
                RectCorners(POINT_HEIGHT / 2 - inset),
                fill = if (isFilled) color else color.withAd(SPENT_POINT_ALPHA),
                stroke = color,
                strokeThickness = if (isFilled) 0.0 else SPENT_POINT_STROKE_THICKNESS,
            ) {
                name = if (isFilled) FILLED else HOLLOW
                xy(index * (POINT_WIDTH + POINT_SPACING) + inset, inset)
            }
        }
    }
}
