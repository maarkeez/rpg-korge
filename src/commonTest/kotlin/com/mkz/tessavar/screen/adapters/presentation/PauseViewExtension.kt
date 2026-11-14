package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun Stage.pauseView(): PauseView? = descendants().filterIsInstance<PauseView>().firstOrNull()

fun PauseView.optionLabels(): List<String> = listOf(resumeButton.confirmButton.text, saveButton.cancelButton.text, quitButton.cancelButton.text)

fun PauseView.isSavedShown(): Boolean = savedText.visible

fun PauseView.savedLabel(): String = savedText.text

suspend fun ViewsForTesting.clickResume(pauseView: PauseView) = clickOn(pauseView.resumeButton.confirmButton)

suspend fun ViewsForTesting.clickSave(pauseView: PauseView) = clickOn(pauseView.saveButton.cancelButton)

suspend fun ViewsForTesting.clickQuit(pauseView: PauseView) = clickOn(pauseView.quitButton.cancelButton)
