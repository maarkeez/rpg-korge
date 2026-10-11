package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class CancelAndConfirmView(
    val onCancelled: () -> Unit,
    val onConfirmed: () -> Unit,
) : Container() {
    internal val cancelButton = CancelButton(onCancelled = { onCancelled() })
    internal val confirmButton = ConfirmButton(onConfirmed = { onConfirmed() })

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
