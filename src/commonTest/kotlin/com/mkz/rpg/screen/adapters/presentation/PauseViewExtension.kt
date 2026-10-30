package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [PauseView].

/** The pause screen of the battle scene, or `null` when the scene is not displayed yet. */
fun Stage.pauseView(): PauseView? = descendants().filterIsInstance<PauseView>().firstOrNull()

/** The labels of the options, from the top, e.g.: `Resume`. */
fun PauseView.optionLabels(): List<String> = listOf(resumeButton.confirmButton.text, saveButton.cancelButton.text, quitButton.cancelButton.text)

suspend fun ViewsForTesting.clickResume(pauseView: PauseView) = clickOn(pauseView.resumeButton.confirmButton)

suspend fun ViewsForTesting.clickSave(pauseView: PauseView) = clickOn(pauseView.saveButton.cancelButton)

suspend fun ViewsForTesting.clickQuit(pauseView: PauseView) = clickOn(pauseView.quitButton.cancelButton)
