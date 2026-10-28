package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

// Page object of [ActionsView].

/** The title, with how many actions there are, e.g.: `Actions (3)`. */
fun ActionsView.titleText(): String = title.text

/** The actions listed, the newest first, e.g.: `shopSlotSelected(3)`. */
fun ActionsView.actionTexts(): List<String> = actionTexts.map { text -> text.text }

/** Whether the actions are listed top down, under the title, all of them inside the panel. */
fun ActionsView.isListedTopDown(): Boolean =
    actionTexts.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } &&
        actionTexts.all { text -> text.isShown() && text.globalPos.y > title.globalPos.y && text.globalPos.y + text.height <= globalPos.y + ActionsView.HEIGHT }

suspend fun ViewsForTesting.clickClear(actionsView: ActionsView) = clickOn(actionsView.clearButton)
