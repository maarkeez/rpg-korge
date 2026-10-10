package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
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
            bgColorOut = UiPalette.secondaryButton
            bgColorOver = UiPalette.secondaryButtonOver
            background.borderColor = UiPalette.panelBorder
            onClick {
                onCancelled()
            }
        }
}
