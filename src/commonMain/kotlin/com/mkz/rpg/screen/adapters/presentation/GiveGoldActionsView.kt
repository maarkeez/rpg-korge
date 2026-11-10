package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class GiveGoldActionsView(
    private val messages: Messages,
    onCancelled: () -> Unit,
    onAll: () -> Unit,
    onGiven: () -> Unit,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val SPACING = 10.0
        private const val BUTTON_WIDTH = (WIDTH - 2 * SPACING) / 3
    }

    private val buttonSize = Size(BUTTON_WIDTH, UIButton.DEFAULT_SIZE.height)
    internal val cancelButton: GameButton =
        gameButton(messages[UiMessage.GIVE_GOLD_CANCEL], buttonSize) {
            onClick { onCancelled() }
        }
    internal val allButton: GameButton =
        gameButton(messages[UiMessage.GIVE_GOLD_ALL], buttonSize) {
            xy(BUTTON_WIDTH + SPACING, 0.0)
            onClick { onAll() }
        }
    internal val giveButton: GameButton =
        gameButton("", buttonSize, color = ConfirmButton.COLOR, overColor = ConfirmButton.PRESSED_COLOR) {
            xy(2 * (BUTTON_WIDTH + SPACING), 0.0)
            onClick { onGiven() }
        }

    fun displayAmount(
        amount: Int,
        maximum: Int,
    ) {
        giveButton.text = messages[UiMessage.GIVE_GOLD_GIVE, "amount" to messages.gold(amount)]
        allButton.enabled = amount < maximum
    }
}
