package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting

suspend fun ViewsForTesting.clickCancel(cancelAndConfirmView: CancelAndConfirmView) = clickCancel(cancelAndConfirmView.cancelButton)

suspend fun ViewsForTesting.clickConfirm(cancelAndConfirmView: CancelAndConfirmView) = clickConfirm(cancelAndConfirmView.confirmButton)
