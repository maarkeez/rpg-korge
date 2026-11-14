package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.GAME_WINDOW_SIZE
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.ClipContainer
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.View
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.clipContainer
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

open class PhoneFrameView : Container() {
    companion object {
        const val BEZEL = 12.0
        val SIZE = Size(GAME_WINDOW_SIZE.width + 2 * BEZEL, GAME_WINDOW_SIZE.height + 2 * BEZEL)
        val BEZEL_COLOR = RGBA(0x0A, 0x0A, 0x0A)

        val SCREEN_COLOR = Colors["#2b2b2b"]

        private const val CORNER_RADIUS = 36.0
    }

    internal val screen: ClipContainer
    internal val screenBackground: SolidRect
    internal var storyView: View? = null
        private set

    init {
        roundRect(SIZE, RectCorners(CORNER_RADIUS), fill = BEZEL_COLOR, stroke = RGBA(0x44, 0x44, 0x44), strokeThickness = 2.0)
        screen = clipContainer(GAME_WINDOW_SIZE).position(BEZEL, BEZEL)
        screenBackground = screen.solidRect(GAME_WINDOW_SIZE, SCREEN_COLOR)
    }

    open fun display(storyView: View) {
        this.storyView?.removeFromParent()
        screen.addChild(storyView)
        storyView.centerOn(screenBackground)
        this.storyView = storyView
    }
}
