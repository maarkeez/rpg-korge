package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting

fun ConfirmButton.buttonText(): String = confirmButton.text

fun ConfirmButton.buttonTextColor(): RGBA = confirmButton.textColor

suspend fun ViewsForTesting.clickConfirm(confirmButton: ConfirmButton) = clickOn(confirmButton.confirmButton)
