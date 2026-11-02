package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [ConfirmDialogView]. Its choices are in the row, in the `PlayerCallToActionView` page object.

/** The confirmation dialog of the scene, or `null` when the scene has none. */
fun Stage.confirmDialogView(): ConfirmDialogView? = descendants().filterIsInstance<ConfirmDialogView>().firstOrNull()

/** The question asked, e.g.: `Quit to the menu? ...`. */
fun ConfirmDialogView.message(): String = messageText.plainText
