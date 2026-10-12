package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [CancelButton].

fun CancelButton.buttonText(): String = cancelButton.text

suspend fun ViewsForTesting.clickCancel(cancelButton: CancelButton) = clickOn(cancelButton.cancelButton)
