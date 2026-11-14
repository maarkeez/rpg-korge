package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class DiscardQuestionView(
    messages: Messages,
    itemName: String,
    onBack: () -> Unit,
    onDiscarded: () -> Unit,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val LINE_HEIGHT = 16.0
        private const val SPACING = 4.0
    }

    internal val questionView = UnitNameView(Size(WIDTH, LINE_HEIGHT))
    internal val rowView =
        ButtonRowView(
            listOf(
                ButtonRowView.Action(messages[UiMessage.ITEM_BUTTONS_BACK], onSelected = onBack),
                ButtonRowView.Action(messages[UiMessage.ITEM_BUTTONS_DISCARD], HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onDiscarded),
            ),
        )

    init {
        addChild(questionView)
        addChild(rowView.xy(0.0, LINE_HEIGHT + SPACING))
        questionView.display(messages[UiMessage.ITEM_BUTTONS_DISCARD_QUESTION, "item" to itemName])
    }
}
