package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class FinishTurnView(
    texts: Texts,
    onTurnFinished: () -> Unit,
) : Container() {
    internal val button =
        gameButton(texts[UiText.FINISH_TURN], Size(390.0, UIButton.DEFAULT_SIZE.height), color = RGBA(0, 136, 255), overColor = RGBA(30, 110, 244)) {
            onClick {
                onTurnFinished()
            }
        }

    /** A disabled button is still displayed, faded, but can not be clicked. */
    var isEnabled: Boolean
        get() = button.enabled
        set(value) {
            button.enabled = value
        }
}
