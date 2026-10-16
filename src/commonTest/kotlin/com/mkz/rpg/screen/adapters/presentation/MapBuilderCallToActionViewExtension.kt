package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [MapBuilderCallToActionView].

/** The labels of the secondary and the primary actions, e.g.: `"Back" to "Export"`, or `null` when none is displayed. */
fun MapBuilderCallToActionView.actionLabels(): Pair<String, String>? = callToAction?.let { view -> view.cancelButton.cancelButton.text to view.confirmButton.confirmButton.text }

/** Clicks the secondary action, on the left: Back or Stay. */
suspend fun ViewsForTesting.clickSecondaryAction(callToActionView: MapBuilderCallToActionView) = clickOn(callToActionView.callToAction!!.cancelButton.cancelButton)

/** Clicks the primary action, on the right: Export or Leave. */
suspend fun ViewsForTesting.clickPrimaryAction(callToActionView: MapBuilderCallToActionView) = clickOn(callToActionView.callToAction!!.confirmButton.confirmButton)
