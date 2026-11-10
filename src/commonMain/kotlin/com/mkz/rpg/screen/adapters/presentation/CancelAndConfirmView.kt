package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class CancelAndConfirmView(
    val onCancelled: () -> Unit,
    val onConfirmed: () -> Unit,
    cancelLabel: String,
    confirmLabel: String,
    confirmColor: RGBA = ConfirmButton.COLOR,
    confirmPressedColor: RGBA = ConfirmButton.PRESSED_COLOR,
) : Container() {
    internal val cancelButton = CancelButton(label = cancelLabel, onCancelled = { onCancelled() })
    internal val confirmButton = ConfirmButton(label = confirmLabel, color = confirmColor, pressedColor = confirmPressedColor, onConfirmed = { onConfirmed() })

    init {
        val layout =
            uiHorizontalStack {
                addChild(cancelButton)
                uiSpacing(Size(10, 0))
                addChild(confirmButton)
            }
        addChild(layout)
    }
}
