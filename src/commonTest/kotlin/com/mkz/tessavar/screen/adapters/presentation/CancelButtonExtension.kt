package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

fun CancelButton.buttonText(): String = cancelButton.text

suspend fun ViewsForTesting.clickCancel(cancelButton: CancelButton) = clickOn(cancelButton.cancelButton)
