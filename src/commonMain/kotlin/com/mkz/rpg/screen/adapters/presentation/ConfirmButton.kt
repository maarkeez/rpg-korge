package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

/** A primary action button. It reads "Confirm" unless another [label] is given. */
class ConfirmButton(
    label: String = "Confirm",
    buttonWidth: Double = 190.0,
    val onConfirmed: () -> Unit,
) : Container() {
    internal val confirmButton: UIButton =
        uiButton {
            text = label
            width = buttonWidth
            bgColorOut = RGBA(0, 136, 255)
            bgColorOver = RGBA(30, 110, 244)
            background.borderColor = Colors.LIGHTGRAY
            onClick {
                onConfirmed()
            }
        }
}
