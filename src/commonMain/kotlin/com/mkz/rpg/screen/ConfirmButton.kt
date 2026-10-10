package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
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
            height = BattleLayout.ACTION_BAR_HEIGHT.toDouble()
            bgColorOut = UiPalette.primaryButton
            bgColorOver = UiPalette.primaryButtonOver
            background.borderColor = UiPalette.panelBorder
            onClick {
                onConfirmed()
            }
        }
}
