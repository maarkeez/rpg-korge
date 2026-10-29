package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_BACKGROUND_COLOR
import korlibs.korge.view.Container
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.onStageResized
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size

/**
 * The Storybook, zoomed and placed on the screen as its [Viewport] says, framed by the background, and the [ZoomBarView]
 * floating at the bottom right of the screen, not zoomed with it, as large on a phone as on a 1280x900 window. Fit is the
 * whole Storybook, as KorGE shows it; Phone zooms so the [phoneArea] fills the screen. The zoom is kept until the screen
 * changes size, which it follows. It is placed at the origin of the stage, so the screen is KorGE's `actualVirtualBounds`.
 */
class ZoomView(
    storybookSize: Size,
) : Container(),
    ZoomBarView.Delegate {
    companion object {
        private const val BAR_MARGIN = 12.0

        // As wide as the tallest screen the Storybook could be fitted in, in its units.
        private const val FRAME_WIDTH = 100_000.0

        // The pixels per inch of a desktop screen, where the bar is drawn at its size.
        private const val DESKTOP_PIXELS_PER_INCH = 96.0
    }

    /** Holds the views of the Storybook, laid out in its own units. */
    val storybook: Container = container()

    // Covers what the views draw out of the Storybook, which the window used to crop, e.g.: the stories of the sidebar
    // scrolled above its list. A clip would not: it does not crop their rounded rectangles.
    internal val frame: Container = container()
    internal val zoomBarView = ZoomBarView()
    internal val viewport = Viewport(storybookSize, Rectangle(Point.ZERO, storybookSize))

    /** The area of the Storybook the Phone preset fills the screen with: the phone frame. */
    var phoneArea: Rectangle = Rectangle(Point.ZERO, storybookSize)

    // How much larger than its size the bar is drawn, so it keeps its size on the screen.
    private var barScale = 1.0

    init {
        frame.solidRect(Size(storybookSize.width + 2 * FRAME_WIDTH, FRAME_WIDTH), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, -FRAME_WIDTH)
        frame.solidRect(Size(storybookSize.width + 2 * FRAME_WIDTH, FRAME_WIDTH), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, storybookSize.height)
        frame.solidRect(Size(FRAME_WIDTH, storybookSize.height), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, 0.0)
        frame.solidRect(Size(FRAME_WIDTH, storybookSize.height), STORYBOOK_BACKGROUND_COLOR).position(storybookSize.width, 0.0)
        zoomBarView.addTo(this)
        zoomBarView.setDelegate(this)
        onStageResized { _, _ ->
            barScale = (virtualPixelsPerInch / DESKTOP_PIXELS_PER_INCH).coerceAtLeast(1.0)
            resize(actualVirtualBounds)
        }
        show()
    }

    /** Follows a new size of the [screen], in the units of the Storybook. */
    fun resize(screen: Rectangle) {
        viewport.resize(screen)
        show()
    }

    override fun zoomOutTapped() {
        viewport.zoomOut()
        show()
    }

    override fun zoomInTapped() {
        viewport.zoomIn()
        show()
    }

    override fun fitTapped() {
        viewport.fit()
        show()
    }

    override fun phoneTapped() {
        viewport.zoomOnto(phoneArea)
        show()
    }

    private fun show() {
        for (view in listOf(storybook, frame)) {
            view.scale = viewport.zoom
            view.position(viewport.position)
        }
        zoomBarView.display(viewport.zoom)
        zoomBarView.scale = barScale
        val screen = viewport.screen
        zoomBarView.position(
            screen.right - (ZoomBarView.SIZE.width + BAR_MARGIN) * barScale,
            screen.bottom - (ZoomBarView.SIZE.height + BAR_MARGIN) * barScale,
        )
    }
}
