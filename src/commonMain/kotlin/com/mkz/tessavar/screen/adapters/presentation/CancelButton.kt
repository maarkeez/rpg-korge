package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class CancelButton(
    label: String,
    buttonWidth: Double = 190.0,
    val onCancelled: () -> Unit,
) : Container() {
    internal val cancelButton: UIButton =
        uiButton {
            text = label
            width = buttonWidth
            bgColorOut = Colors.DIMGRAY
            bgColorOver = RGBA(85, 85, 85)
            background.borderColor = Colors.LIGHTGRAY
            onClick {
                onCancelled()
            }
        }
}
