package com.mkz.rpg.storybook.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.math.roundToInt

/**
 * The zoom of the Storybook, floating over it: `-`, the zoom (`100%` at Fit), `+`, `Fit` and `Phone`. Its buttons are
 * larger than the other buttons of the Storybook, to be tapped with a finger. Tapping one tells the delegate.
 */
open class ZoomBarView : Container() {
    companion object {
        private const val BUTTON_HEIGHT = 40.0
        private const val PADDING = 6.0
        private const val GAP = 6.0
        private val STEP_SIZE = Size(BUTTON_HEIGHT, BUTTON_HEIGHT)
        private val PRESET_SIZE = Size(72.0, BUTTON_HEIGHT)
        private val ZOOM_SIZE = Size(64.0, BUTTON_HEIGHT)
        val SIZE = Size(2 * PADDING + 2 * STEP_SIZE.width + ZOOM_SIZE.width + 2 * PRESET_SIZE.width + 4 * GAP, 2 * PADDING + BUTTON_HEIGHT)
    }

    interface Delegate {
        fun zoomOutTapped()

        fun zoomInTapped()

        fun fitTapped()

        fun phoneTapped()
    }

    private var delegate: Delegate? = null

    internal val zoomOutButton: StorybookButton
    private val zoomArea: SolidRect
    internal val zoomText: Text
    internal val zoomInButton: StorybookButton
    internal val fitButton: StorybookButton
    internal val phoneButton: StorybookButton

    init {
        roundRect(SIZE, RectCorners(8.0), fill = SidebarView.BACKGROUND_COLOR, stroke = StorybookButton.HOVER_COLOR, strokeThickness = 1.0)
        var x = PADDING
        zoomOutButton = StorybookButton("-", STEP_SIZE) { delegate?.zoomOutTapped() }.addTo(this).position(x, PADDING)
        x += STEP_SIZE.width + GAP
        zoomArea = solidRect(ZOOM_SIZE, SidebarView.BACKGROUND_COLOR).position(x, PADDING)
        zoomText = text("", textSize = StorybookButton.TEXT_SIZE)
        x += ZOOM_SIZE.width + GAP
        zoomInButton = StorybookButton("+", STEP_SIZE) { delegate?.zoomInTapped() }.addTo(this).position(x, PADDING)
        x += STEP_SIZE.width + GAP
        fitButton = StorybookButton("Fit", PRESET_SIZE) { delegate?.fitTapped() }.addTo(this).position(x, PADDING)
        x += PRESET_SIZE.width + GAP
        phoneButton = StorybookButton("Phone", PRESET_SIZE) { delegate?.phoneTapped() }.addTo(this).position(x, PADDING)
        display(zoom = 1.0)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Shows the [zoom], 1 at Fit, as a percentage, e.g.: `150%`. */
    open fun display(zoom: Double) {
        zoomText.text = "${(zoom * 100).roundToInt()}%"
        zoomText.centerOn(zoomArea)
    }
}
