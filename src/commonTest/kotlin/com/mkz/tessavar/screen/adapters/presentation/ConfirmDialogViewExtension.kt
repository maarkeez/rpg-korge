package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

fun Stage.confirmDialogView(): ConfirmDialogView? = descendants().filterIsInstance<ConfirmDialogView>().firstOrNull()

fun ConfirmDialogView.message(): String = messageText.plainText
