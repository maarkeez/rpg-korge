package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/** A secondary action on the left and a primary one on the right, reading "Cancel" and "Confirm" unless other labels are given. */
class CancelAndConfirmView(
    val onCancelled: () -> Unit,
    val onConfirmed: () -> Unit,
    cancelLabel: String = "Cancel",
    confirmLabel: String = "Confirm",
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
