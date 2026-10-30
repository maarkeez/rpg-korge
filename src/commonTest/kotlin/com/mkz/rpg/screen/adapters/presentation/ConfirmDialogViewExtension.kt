package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [ConfirmDialogView]. Uses the `CancelAndConfirmView` page object.

/** The confirmation dialog of the scene, or `null` when the scene has none. */
fun Stage.confirmDialogView(): ConfirmDialogView? = descendants().filterIsInstance<ConfirmDialogView>().firstOrNull()

/** The question asked, e.g.: `Quit to the menu? ...`. */
fun ConfirmDialogView.message(): String = messageText.plainText

/** The labels of both choices, the secondary one first, e.g.: `Cancel` and `Quit to menu`. */
fun ConfirmDialogView.choiceLabels(): List<String> = listOfNotNull(cancelAndConfirmView?.cancelButton?.buttonText(), cancelAndConfirmView?.confirmButton?.buttonText())

suspend fun ViewsForTesting.clickCancel(confirmDialogView: ConfirmDialogView) = clickCancel(requireNotNull(confirmDialogView.cancelAndConfirmView) { "The dialog asks nothing" })

suspend fun ViewsForTesting.clickConfirm(confirmDialogView: ConfirmDialogView) = clickConfirm(requireNotNull(confirmDialogView.cancelAndConfirmView) { "The dialog asks nothing" })

/** Chooses the secondary action, e.g.: Cancel. */
fun ConfirmDialogView.chooseCancel() = requireNotNull(cancelAndConfirmView) { "The dialog asks nothing" }.onCancelled()

/** Chooses the primary action, e.g.: Quit to menu. */
fun ConfirmDialogView.chooseConfirm() = requireNotNull(cancelAndConfirmView) { "The dialog asks nothing" }.onConfirmed()
