package com.mkz.rpg.storybook.adapters.presentation

import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size

class Viewport(
    private val content: Size,
    screen: Rectangle,
) {
    companion object {
        const val MAXIMUM_ZOOM = 4.0
        const val ZOOM_STEP = 1.25

        private const val SAME_ZOOM_TOLERANCE = 1e-6
    }

    var screen: Rectangle = screen
        private set

    var zoom: Double = 1.0
        private set

    var position: Point = Point.ZERO
        private set

    val isFit: Boolean get() = zoom < 1.0 + SAME_ZOOM_TOLERANCE

    init {
        fit()
    }

    fun fit() {
        zoom = 1.0
        position = screen.center - Point(content.width, content.height) / 2.0
    }

    fun zoomIn(around: Point = screen.center) = zoomTo(zoom * ZOOM_STEP, around)

    fun zoomOut(around: Point = screen.center) = zoomTo(zoom / ZOOM_STEP, around)

    fun zoomTo(
        zoom: Double,
        around: Point = screen.center,
    ) {
        val pointOfContent = (around - position) / this.zoom
        this.zoom = zoom.coerceIn(1.0, MAXIMUM_ZOOM)
        position = around - pointOfContent * this.zoom
        keepOnScreen()
    }

    fun zoomOnto(area: Rectangle) {
        zoom = minOf(screen.width / area.width, screen.height / area.height).coerceIn(1.0, MAXIMUM_ZOOM)
        position = screen.center - area.center * zoom
        keepOnScreen()
    }

    fun panBy(delta: Point) {
        position += delta
        keepOnScreen()
    }

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
