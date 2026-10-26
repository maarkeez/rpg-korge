package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/** A secondary action on the left and a primary one on the right, e.g.: Cancel and Confirm. */
class CancelAndConfirmView(
    val onCancelled: () -> Unit,
    val onConfirmed: () -> Unit,
    cancelLabel: String,
    confirmLabel: String,
) : Container() {
    internal val cancelButton = CancelButton(label = cancelLabel, onCancelled = { onCancelled() })
    internal val confirmButton = ConfirmButton(label = confirmLabel, onConfirmed = { onConfirmed() })

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
