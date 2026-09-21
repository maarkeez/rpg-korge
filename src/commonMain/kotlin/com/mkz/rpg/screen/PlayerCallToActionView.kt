package com.mkz.rpg.screen

import korlibs.korge.view.Container
import korlibs.korge.view.View

class PlayerCallToActionView : Container() {
    fun hide() {
        visible = false
    }

    fun displayFinishTurn(onTurnFinished: () -> Unit) {
        val finishTurnView = FinishTurnView(onTurnFinished = onTurnFinished)
        display(finishTurnView)
    }

    fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        val cancelAndConfirmView =
            CancelAndConfirmView(
                onCancelled = onCancelled,
                onConfirmed = onConfirmed,
            )
        display(cancelAndConfirmView)
    }

    private fun display(view: View) {
        val viewName = "call-to-action"
        view.name = viewName
        findViewByName(viewName)?.removeFromParent()
        addChild(view)
    }
}
