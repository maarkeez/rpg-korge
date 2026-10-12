package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting

// Page object of [CancelAndConfirmView]. Uses the `CancelButton` and `ConfirmButton` page objects.

suspend fun ViewsForTesting.clickCancel(cancelAndConfirmView: CancelAndConfirmView) = clickCancel(cancelAndConfirmView.cancelButton)

suspend fun ViewsForTesting.clickConfirm(cancelAndConfirmView: CancelAndConfirmView) = clickConfirm(cancelAndConfirmView.confirmButton)
