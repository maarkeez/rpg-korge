package com.mkz.rpg.storybook.adapters.presentation

import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size

/**
 * How the Storybook is zoomed and placed on the screen, in the units of the Storybook. At Fit, the zoom is 1 and the
 * whole Storybook is in the middle of the screen, as KorGE shows it; it can be zoomed up to [MAXIMUM_ZOOM] times. The
 * Storybook never leaves the screen: on a side where it is larger than the screen, it is moved at most to its edge, and
 * on a side where it is smaller, it is centred.
 */
class Viewport(
    private val content: Size,
    screen: Rectangle,
) {
    companion object {
        const val MAXIMUM_ZOOM = 4.0
        const val ZOOM_STEP = 1.25

        // Below this difference, two zooms are the same, e.g.: Fit after zooming in and out.
        private const val EPSILON = 1e-6
    }

    /** The screen, in the units of the Storybook. */
    var screen: Rectangle = screen
        private set

    /** 1 at Fit, at most [MAXIMUM_ZOOM]. */
    var zoom: Double = 1.0
        private set

    /** Where the top left corner of the Storybook is on the screen. */
    var position: Point = Point.ZERO
        private set

    val isFit: Boolean get() = zoom < 1.0 + EPSILON

    init {
        fit()
    }

    /** The whole Storybook, in the middle of the screen. */
    fun fit() {
        zoom = 1.0
        position = screen.center - Point(content.width, content.height) / 2.0
    }

    fun zoomIn(around: Point = screen.center) = zoomTo(zoom * ZOOM_STEP, around)

    fun zoomOut(around: Point = screen.center) = zoomTo(zoom / ZOOM_STEP, around)

    /** Zooms to [zoom], within its range, keeping the point of the Storybook under [around] where it is on the screen. */
    fun zoomTo(
        zoom: Double,
        around: Point = screen.center,
    ) {
        val pointOfContent = (around - position) / this.zoom
        this.zoom = zoom.coerceIn(1.0, MAXIMUM_ZOOM)
        position = around - pointOfContent * this.zoom
        keepOnScreen()
    }

    /** Zooms so the [area] of the Storybook fills the screen, within the range of the zoom, in the middle of it. */
    fun zoomOnto(area: Rectangle) {
        zoom = minOf(screen.width / area.width, screen.height / area.height).coerceIn(1.0, MAXIMUM_ZOOM)
        position = screen.center - area.center * zoom
        keepOnScreen()
    }

    /** Moves the Storybook by [delta] on the screen. */
    fun panBy(delta: Point) {
        position += delta
        keepOnScreen()
    }

    /** Follows a new size of the screen: Fit stays at Fit; otherwise the zoom and the point in the middle of the screen are kept. */
    fun resize(screen: Rectangle) {
        val pointOfContent = (this.screen.center - position) / zoom
        this.screen = screen
        if (isFit) {
            fit()
        } else {
            position = screen.center - pointOfContent * zoom
            keepOnScreen()
        }
    }

    private fun keepOnScreen() {
        position =
            Point(
                keepOnScreen(position.x, content.width * zoom, screen.left, screen.right),
                keepOnScreen(position.y, content.height * zoom, screen.top, screen.bottom),
            )
    }

    private fun keepOnScreen(
        start: Double,
        length: Double,
        screenStart: Double,
        screenEnd: Double,
    ): Double =
        if (length <= screenEnd - screenStart) {
            (screenStart + screenEnd - length) / 2
        } else {
            start.coerceIn(screenEnd - length, screenStart)
        }
}
