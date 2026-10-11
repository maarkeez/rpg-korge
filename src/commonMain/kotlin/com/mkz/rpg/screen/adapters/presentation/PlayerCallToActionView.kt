package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

class PlayerCallToActionView : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    private var onTurnFinished: (() -> Unit)? = null
    private var isFinishTurnEnabled = true

    fun hide() {
        visible = false
    }

    fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
        val finishTurnView = FinishTurnView(onTurnFinished = onTurnFinished)
        finishTurnView.isEnabled = isFinishTurnEnabled
        display(finishTurnView)
    }

    /** Enables or disables the finish turn, also when it is displayed again later (e.g.: after cancelling a cast). */
    fun setFinishTurnEnabled(enabled: Boolean) {
        isFinishTurnEnabled = enabled
        (callToAction as? FinishTurnView)?.isEnabled = enabled
    }

    fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        val cancelAndConfirmView =
            CancelAndConfirmView(
                onCancelled = {
                    onCancelled()
                    displayFinishTurn(onTurnFinished = onTurnFinished!!)
                },
                onConfirmed = {
                    onConfirmed()
                    displayFinishTurn(onTurnFinished = onTurnFinished!!)
                },
            )
        display(cancelAndConfirmView)
    }

    internal val callToAction: View?
        get() = findViewByName(CALL_TO_ACTION)

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
