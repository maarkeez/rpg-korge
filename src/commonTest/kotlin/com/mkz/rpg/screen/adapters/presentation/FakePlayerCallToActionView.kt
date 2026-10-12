package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the call to action view to display, instead of drawing it.
// The player actions are simulated by calling the callbacks it was given (e.g.: `onTurnFinished`).
class FakePlayerCallToActionView : PlayerCallToActionView() {
    var onTurnFinished: (() -> Unit)? = null
        private set
    var onCancelled: (() -> Unit)? = null
        private set
    var onConfirmed: (() -> Unit)? = null
        private set
    val finishTurnEnabledChanges = mutableListOf<Boolean>()
    var hideCount = 0
        private set

    val isFinishTurnEnabled: Boolean
        get() = finishTurnEnabledChanges.lastOrNull() ?: false

    override fun hide() {
        hideCount++
    }

    override fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
    }

    override fun setFinishTurnEnabled(enabled: Boolean) {
        finishTurnEnabledChanges += enabled
    }

    override fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        this.onCancelled = onCancelled
        this.onConfirmed = onConfirmed
    }
}
