package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.input.onClick
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

class FinishTurnView(
    onTurnFinished: () -> Unit,
) : Container() {
    private val button =
        uiButton("Finish turn")
            .also { button ->
                button.width = 390.0
                button.bgColorOut = UiPalette.primaryButton
                button.bgColorOver = UiPalette.primaryButtonOver
                button.background.borderColor = UiPalette.panelBorder

                button.onClick {
                    onTurnFinished()
                }
            }
}
