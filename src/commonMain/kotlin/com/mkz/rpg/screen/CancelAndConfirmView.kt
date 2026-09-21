package com.mkz.rpg.screen

import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class CancelAndConfirmView(
    val onCancelled: () -> Unit,
    val onConfirmed: () -> Unit,
) : Container() {
    init {
        val cancelButton = CancelButton(onCancelled = { onCancelled() })
        val confirmButton = ConfirmButton(onConfirmed = { onConfirmed() })
        val layout =
            uiHorizontalStack {
                addChild(cancelButton)
                uiSpacing(Size(10, 0))
                addChild(confirmButton)
            }
        addChild(layout)
    }
}
