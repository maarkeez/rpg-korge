package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [ConfirmButton].

fun ConfirmButton.buttonText(): String = confirmButton.text

suspend fun ViewsForTesting.clickConfirm(confirmButton: ConfirmButton) = clickOn(confirmButton.confirmButton)
