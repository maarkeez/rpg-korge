package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

/** The map builder actions: exporting the map, or going back to the menu once the player confirms leaving it. */
open class MapBuilderCallToActionView : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    open fun displayExportAndBack(
        onExported: () -> Unit,
        onBack: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onBack, onConfirmed = onExported, cancelLabel = "Back", confirmLabel = "Export"))
    }

    /** Asks the player to confirm leaving, as the map is lost. */
    open fun displayLeaveConfirmation(
        onLeft: () -> Unit,
        onStayed: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onStayed, onConfirmed = onLeft, cancelLabel = "Stay", confirmLabel = "Leave"))
    }

    internal val callToAction: CancelAndConfirmView?
        get() = findViewByName(CALL_TO_ACTION) as? CancelAndConfirmView

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
