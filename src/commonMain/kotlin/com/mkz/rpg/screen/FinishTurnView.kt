package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.input.onClick
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container

/** Floating bottom-right button. */
class FinishTurnView(
    onTurnFinished: () -> Unit,
) : Container() {
    val button =
        uiButton("Finish turn")
            .also { button ->
                button.width = BattleLayout.FINISH_TURN_WIDTH.toDouble()
                button.height = BattleLayout.ACTION_BAR_HEIGHT.toDouble()
                button.x = (BattleLayout.SCREEN_WIDTH - BattleLayout.FINISH_TURN_WIDTH - BattleLayout.FINISH_TURN_MARGIN).toDouble()
                button.bgColorOut = UiPalette.primaryButton
                button.bgColorOver = UiPalette.primaryButtonOver
                button.background.borderColor = UiPalette.panelBorder

                button.onClick {
                    onTurnFinished()
                }
            }
}
