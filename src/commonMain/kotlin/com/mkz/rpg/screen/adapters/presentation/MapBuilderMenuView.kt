package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.math.geom.Point
import korlibs.math.geom.Size

/**
 * The map builder hamburger menu: a button which opens a list of options below it, over the screen. Choosing an
 * option, the button again, or anywhere else closes it.
 */
open class MapBuilderMenuView : Container() {
    companion object {
        const val BUTTON_WIDTH = 36.0
        const val BUTTON_HEIGHT = 30.0
        const val BAR_WIDTH = 20.0
        const val BAR_HEIGHT = 3.0
        const val BAR_GAP = 4.0
        const val OPTION_WIDTH = 150.0
        const val OPTION_HEIGHT = 32.0
        const val DROPDOWN_GAP = 4.0
    }

    data class Option(
        val label: String,
        val onSelected: () -> Unit,
    )

    private var options: List<Option> = emptyList()

    // Catches the taps out of the open menu to close it, covering the whole screen.
    internal var outsideTapCatcher: SolidRect? = null
    internal var dropdown: Container? = null
    internal val optionButtons = mutableListOf<UIButton>()

    internal val menuButton =
        uiButton(label = "", size = Size(BUTTON_WIDTH, BUTTON_HEIGHT)) {
            bgColorOut = Colors.TRANSPARENT
            bgColorOver = Colors.DIMGRAY
            background.borderColor = Colors.TRANSPARENT
            repeat(3) { bar ->
                solidRect(Size(BAR_WIDTH, BAR_HEIGHT), Colors.WHITE) {
                    x = (BUTTON_WIDTH - BAR_WIDTH) / 2
                    y = (BUTTON_HEIGHT - 3 * BAR_HEIGHT - 2 * BAR_GAP) / 2 + bar * (BAR_HEIGHT + BAR_GAP)
                }
            }
            onClick { if (isOpen) close() else open() }
        }

    val isOpen: Boolean
        get() = dropdown != null

    /** The options the menu lists when it is opened, in order. */
    open fun displayOptions(options: List<Option>) {
        this.options = options
        if (isOpen) {
            close()
            open()
        }
    }

    open fun open() {
        if (isOpen) return
        val origin = localToGlobal(Point(0, 0))
        outsideTapCatcher =
            solidRect(GAME_WINDOW_SIZE, Colors.TRANSPARENT) {
                x = -origin.x
                y = -origin.y
                onClick { close() }
            }
        // Kept over the catcher, so the menu button still toggles the menu.
        menuButton.removeFromParent()
        addChild(menuButton)
        dropdown =
            container {
                x = BUTTON_WIDTH - OPTION_WIDTH
                y = BUTTON_HEIGHT + DROPDOWN_GAP
                uiVerticalStack(padding = 0.0) {
                    options.forEach { option ->
                        optionButtons +=
                            uiButton(label = option.label, size = Size(OPTION_WIDTH, OPTION_HEIGHT)) {
                                bgColorOut = Colors.DIMGRAY
                                background.borderColor = Colors.LIGHTGRAY
                                onClick {
                                    close()
                                    option.onSelected()
                                }
                            }
                    }
                }
            }
    }

    open fun close() {
        outsideTapCatcher?.removeFromParent()
        outsideTapCatcher = null
        dropdown?.removeFromParent()
        dropdown = null
        optionButtons.clear()
    }
}
