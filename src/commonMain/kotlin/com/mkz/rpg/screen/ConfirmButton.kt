package com.mkz.rpg.screen

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class ConfirmButton(
    val onConfirmed: () -> Unit,
) : Container() {
    private val confirmButton: UIButton =
        uiButton {
            text = "Confirm"
            width = 190.0
            bgColorOut = RGBA(0, 136, 255)
            bgColorOver = RGBA(30, 110, 244)
            background.borderColor = Colors.LIGHTGRAY
            onClick {
                onConfirmed()
            }
        }
}
