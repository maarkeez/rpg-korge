package com.mkz.rpg.screen

import korlibs.korge.view.Container
import korlibs.korge.view.View

class PlayerCallToActionView : Container() {
    private var onTurnFinished: (() -> Unit)? = null

    private var hidden = false
    private var playbackLocked = false

    fun hide() {
        hidden = true
        visible = false
    }

    /** End Turn, Cancel and Confirm are hidden while feedback plays, because taps are ignored then. */
    fun setPlaybackLocked(locked: Boolean) {
        playbackLocked = locked
        visible = !hidden && !locked
    }

    fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
        val finishTurnView = FinishTurnView(onTurnFinished = onTurnFinished)
        display(finishTurnView)
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

    private fun display(view: View) {
        val viewName = "call-to-action"
        view.name = viewName
        findViewByName(viewName)?.removeFromParent()
        addChild(view)
    }
}
