package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.GameButton
import com.mkz.tessavar.shared.adapters.presentation.gameButton
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class ButtonRowView(
    actions: List<Action>,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val SPACING = 10.0
    }

    data class Action(
        val label: String,
        val color: RGBA = GameButton.GREY,
        val pressedColor: RGBA = GameButton.GREY_OVER,
        val isEnabled: Boolean = true,
        val onSelected: () -> Unit,
    )

    internal val buttons: List<GameButton>

    init {
        val buttonWidth = (WIDTH - SPACING * (actions.size - 1)) / actions.size
        buttons =
            actions.mapIndexed { index, action ->
                gameButton(action.label, Size(buttonWidth, UIButton.DEFAULT_SIZE.height), action.color, action.pressedColor) {
                    xy(index * (buttonWidth + SPACING), 0.0)
                    enabled = action.isEnabled
                    onClick { if (action.isEnabled) action.onSelected() }
                }
            }
    }
}
