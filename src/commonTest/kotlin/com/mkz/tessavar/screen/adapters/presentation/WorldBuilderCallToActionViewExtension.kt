package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

fun WorldBuilderCallToActionView.actionLabels(): Pair<String, String>? = callToAction?.let { view -> view.cancelButton.cancelButton.text to view.confirmButton.confirmButton.text }

suspend fun ViewsForTesting.clickSecondaryAction(callToActionView: WorldBuilderCallToActionView) = clickOn(callToActionView.callToAction!!.cancelButton.cancelButton)

suspend fun ViewsForTesting.clickPrimaryAction(callToActionView: WorldBuilderCallToActionView) = clickOn(callToActionView.callToAction!!.confirmButton.confirmButton)
