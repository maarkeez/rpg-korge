package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.Circle
import korlibs.korge.view.Container
import korlibs.korge.view.circle

/**
 * Displays an amount out of a maximum without text: one circle per point, filled while it remains and hollow once it
 * has been spent, e.g.: ●●○ for 2 out of 3.
 */
class PointsView(
    private val color: RGBA,
) : Container() {
    companion object {
        const val FILLED = "FILLED"
        const val HOLLOW = "HOLLOW"

        const val POINT_RADIUS = 5.0
        const val POINT_SPACING = 4.0
        private const val HOLLOW_POINT_STROKE_THICKNESS = 1.5
    }

    internal val points: List<Circle>
        get() = children.filterIsInstance<Circle>()

    fun display(
        remaining: Int,
        maximum: Int,
    ) {
        removeChildren()
        repeat(maximum) { index ->
            val isFilled = index < remaining
            // The outline is drawn half outside the circle, so hollow points are smaller to look as big as filled ones.
            val inset = if (isFilled) 0.0 else HOLLOW_POINT_STROKE_THICKNESS / 2
            circle(
                radius = POINT_RADIUS - inset,
                fill = if (isFilled) color else Colors.TRANSPARENT,
                stroke = color,
                strokeThickness = if (isFilled) 0.0 else HOLLOW_POINT_STROKE_THICKNESS,
            ) {
                name = if (isFilled) FILLED else HOLLOW
                x = index * (POINT_RADIUS * 2 + POINT_SPACING) + inset
                y = inset
            }
        }
    }
}
