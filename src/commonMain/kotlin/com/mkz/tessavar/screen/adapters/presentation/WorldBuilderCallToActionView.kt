package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

open class WorldBuilderCallToActionView(
    private val messages: Messages,
) : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    open fun displayExportAndBack(
        onExported: () -> Unit,
        onBack: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onBack, onConfirmed = onExported, cancelLabel = messages[UiMessage.WORLD_BUILDER_BACK], confirmLabel = messages[UiMessage.WORLD_BUILDER_EXPORT]))
    }

    open fun displayLeaveConfirmation(
        onLeft: () -> Unit,
        onStayed: () -> Unit,
    ) {
        display(CancelAndConfirmView(onCancelled = onStayed, onConfirmed = onLeft, cancelLabel = messages[UiMessage.WORLD_BUILDER_STAY], confirmLabel = messages[UiMessage.WORLD_BUILDER_LEAVE]))
    }

    internal val callToAction: CancelAndConfirmView?
        get() = findViewByName(CALL_TO_ACTION) as? CancelAndConfirmView

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
