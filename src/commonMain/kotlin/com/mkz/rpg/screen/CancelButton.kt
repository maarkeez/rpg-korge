package com.mkz.rpg.screen

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class CancelButton(
    val onCancelled: () -> Unit,
) : Container() {
    private val cancelButton: UIButton =
        uiButton {
            text = "Cancel"
            width = 190.0
            bgColorOut = Colors.DIMGRAY
            bgColorOver = RGBA(85, 85, 85)
            background.borderColor = Colors.LIGHTGRAY
            onClick {
                onCancelled()
            }
        }
}
