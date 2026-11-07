package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

/** The world builder actions: exporting the world, or going back to the menu once the player confirms leaving it. */
open class WorldBuilderCallToActionView(
    private val texts: Texts,
) : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    open fun displayExportAndBack(
        onExported: () -> Unit,
        onBack: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onBack, onConfirmed = onExported, cancelLabel = texts[UiText.WORLD_BUILDER_BACK], confirmLabel = texts[UiText.WORLD_BUILDER_EXPORT]))
    }

    /** Asks the player to confirm leaving, as the world is lost. */
    open fun displayLeaveConfirmation(
        onLeft: () -> Unit,
        onStayed: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onStayed, onConfirmed = onLeft, cancelLabel = texts[UiText.WORLD_BUILDER_STAY], confirmLabel = texts[UiText.WORLD_BUILDER_LEAVE]))
    }

    internal val callToAction: CancelAndConfirmView?
        get() = findViewByName(CALL_TO_ACTION) as? CancelAndConfirmView

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
