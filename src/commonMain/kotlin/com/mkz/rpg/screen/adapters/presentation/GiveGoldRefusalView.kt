package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/** Why no gold can be given, read after tapping the faded gold button, with OK. */
class GiveGoldRefusalView(
    message: String,
    okLabel: String,
    onDismissed: () -> Unit,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
    }

    internal val messageView = UnitNameView(Size(WIDTH, 16))
    internal lateinit var okButton: GameButton

    init {
        uiVerticalStack(padding = 4.0) {
            addChild(messageView)
            okButton =
                gameButton(okLabel, Size(WIDTH, UIButton.DEFAULT_SIZE.height)) {
                    onClick { onDismissed() }
                }
        }
        messageView.display(message)
    }
}
