package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class ConfirmButton(
    label: String,
    buttonWidth: Double = 190.0,
    color: RGBA = COLOR,
    pressedColor: RGBA = PRESSED_COLOR,
    textColor: RGBA = Colors.WHITE,
    val onConfirmed: () -> Unit,
) : Container() {
    companion object {
        val COLOR = RGBA(0, 136, 255)
        val PRESSED_COLOR = RGBA(30, 110, 244)
    }

    internal val confirmButton: UIButton =
        uiButton {
            text = label
            this.textColor = textColor
            width = buttonWidth
            bgColorOut = color
            bgColorOver = pressedColor
            background.borderColor = Colors.LIGHTGRAY
            onClick {
                onConfirmed()
            }
        }
}
