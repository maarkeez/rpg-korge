package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
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

/**
 * A phone, holding the view of the story on show at its real size, in the middle of a screen as large as the game
 * window (390x844). The screen has the background of the game, and crops what does not fit in it.
 */
open class PhoneFrameView : Container() {
    companion object {
        const val BEZEL = 12.0
        val SIZE = Size(GAME_WINDOW_SIZE.width + 2 * BEZEL, GAME_WINDOW_SIZE.height + 2 * BEZEL)
        val BEZEL_COLOR = RGBA(0x0A, 0x0A, 0x0A)

        // The background of the game window.
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

    /** Shows the view in the middle of the screen, in place of the one shown. */
    open fun display(storyView: View) {
        this.storyView?.removeFromParent()
        screen.addChild(storyView)
        storyView.centerOn(screenBackground)
        this.storyView = storyView
    }
}
