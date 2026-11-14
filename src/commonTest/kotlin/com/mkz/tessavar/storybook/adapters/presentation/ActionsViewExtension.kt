package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

fun ActionsView.titleText(): String = title.text

fun ActionsView.emptyHintText(): String? = emptyHint.text.takeIf { emptyHint.visible }

fun ActionsView.actionTexts(): List<String> = actionTexts.map { text -> text.text }

fun ActionsView.isListedTopDown(): Boolean =
    actionTexts.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } &&
        actionTexts.all { text -> text.isShown() && text.globalPos.y > title.globalPos.y && text.globalPos.y + text.height <= globalPos.y + ActionsView.HEIGHT }

suspend fun ViewsForTesting.clickClear(actionsView: ActionsView) = clickOn(actionsView.clearButton)
