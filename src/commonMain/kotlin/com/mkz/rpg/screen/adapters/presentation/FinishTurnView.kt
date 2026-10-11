package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class FinishTurnView(
    onTurnFinished: () -> Unit,
) : Container() {
    internal val button =
        uiButton("Finish turn")
            .also { button ->
                button.width = 390.0
                button.bgColorOut = RGBA(0, 136, 255)
                button.bgColorOver = RGBA(30, 110, 244)
                button.background.borderColor = Colors.LIGHTGRAY

                button.onClick {
                    onTurnFinished()
                }
            }

    /** A disabled button is still displayed, greyed out, but can not be clicked. */
    var isEnabled: Boolean
        get() = button.enabled
        set(value) {
            button.enabled = value
        }
}
