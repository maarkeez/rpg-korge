package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.xy
import korlibs.math.geom.Size

/**
 * Asks the player whether to discard an item, in place of its actions: the question takes the line of its name and sell price,
 * and Back and Discard, in red, the row below.
 */
class DiscardQuestionView(
    texts: Texts,
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
        ActionRowView(
            listOf(
                ActionRowView.Action(texts[UiText.ITEM_ACTIONS_BACK], onSelected = onBack),
                ActionRowView.Action(texts[UiText.ITEM_ACTIONS_DISCARD], HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onDiscarded),
            ),
        )

    init {
        addChild(questionView)
        addChild(rowView.xy(0.0, LINE_HEIGHT + SPACING))
        questionView.display(texts[UiText.ITEM_ACTIONS_DISCARD_QUESTION, "item" to itemName])
    }
}
