package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_BACKGROUND_COLOR
import korlibs.korge.input.onMagnify
import korlibs.korge.input.onMouseDrag
import korlibs.korge.input.onScroll
import korlibs.korge.input.touch
import korlibs.korge.view.Container
import korlibs.korge.view.View
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.hasAncestor
import korlibs.korge.view.onStageResized
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import kotlin.math.pow

class ZoomView(
    storybookSize: Size,
) : Container(),
    ZoomBarView.Delegate {
    companion object {
        private const val BAR_MARGIN = 12.0

        private const val FRAME_WIDTH = 100_000.0

        private const val WHEEL_NOTCH_PIXELS = 5.0

        private const val DESKTOP_PIXELS_PER_INCH = 96.0
    }

    internal val backdrop = solidRect(storybookSize, STORYBOOK_BACKGROUND_COLOR)

    val storybook: Container = container()

    internal val frame: Container = container()
    internal val zoomBarView = ZoomBarView()
    internal val viewport = Viewport(storybookSize, Rectangle(Point.ZERO, storybookSize))

    var dragOwners: List<View> = emptyList()

    var phoneArea: Rectangle = Rectangle(Point.ZERO, storybookSize)

    private var barScale = 1.0

    private var pinch: Pair<Point, Double>? = null

    private var hasPinched = false

    init {
        frame.solidRect(Size(storybookSize.width + 2 * FRAME_WIDTH, FRAME_WIDTH), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, -FRAME_WIDTH)
        frame.solidRect(Size(storybookSize.width + 2 * FRAME_WIDTH, FRAME_WIDTH), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, storybookSize.height)
        frame.solidRect(Size(FRAME_WIDTH, storybookSize.height), STORYBOOK_BACKGROUND_COLOR).position(-FRAME_WIDTH, 0.0)
        frame.solidRect(Size(FRAME_WIDTH, storybookSize.height), STORYBOOK_BACKGROUND_COLOR).position(storybookSize.width, 0.0)
        zoomBarView.addTo(this)
        zoomBarView.setDelegate(this)
        var isPanning = false
        var lastDrag = Point.ZERO
        onMouseDrag { event ->
            if (event.start) {
                val startView = storybook.hitTest(Point(event.sx, event.sy))
                isPanning = dragOwners.none { owner -> startView?.hasAncestor(owner) == true }
                lastDrag = Point.ZERO
                hasPinched = false
            }
            if (hasPinched) isPanning = false
            if (!isPanning) return@onMouseDrag
            val drag = Point(event.dx, event.dy)
            viewport.panBy(drag - lastDrag)
            lastDrag = drag
            show()
        }
        touch {
            updateAll { touches ->
                val fingers = touches.infos
                if (fingers.size < 2) {
                    pinch = null
                    return@updateAll
                }
                val middle = (fingers[0].global + fingers[1].global) / 2.0
                val distance = Point.distance(fingers[0].global, fingers[1].global)
                pinch?.let { (lastMiddle, lastDistance) ->
                    if (lastDistance > 0) viewport.zoomTo(viewport.zoom * distance / lastDistance, around = lastMiddle)
                    viewport.panBy(middle - lastMiddle)
                    show()
                }
                pinch = middle to distance
                hasPinched = true
            }
        }
        onMagnify { gesture ->
            viewport.zoomTo(viewport.zoom * (1 + gesture.amount), around = gesture.views.globalMousePos)
            show()
        }
        onScroll { event ->
            if (!event.isCtrlDown) return@onScroll
            viewport.zoomTo(viewport.zoom * Viewport.ZOOM_STEP.pow(-event.scrollDeltaYPixels / WHEEL_NOTCH_PIXELS), around = event.currentPosGlobal)
            show()
        }
        onStageResized { _, _ ->
            barScale = (virtualPixelsPerInch / DESKTOP_PIXELS_PER_INCH).coerceAtLeast(1.0)
            resize(actualVirtualBounds)
        }
        show()
    }

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
        for (view in listOf(backdrop, storybook, frame)) {
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
