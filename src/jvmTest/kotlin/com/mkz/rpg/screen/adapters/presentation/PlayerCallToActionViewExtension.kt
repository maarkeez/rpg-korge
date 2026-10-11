package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting

// Page object of [PlayerCallToActionView]. Uses the page objects of the call to action it displays.

fun PlayerCallToActionView.isDisplayingFinishTurn(): Boolean = visible && callToAction is FinishTurnView

fun PlayerCallToActionView.isDisplayingCancelAndConfirm(): Boolean = visible && callToAction is CancelAndConfirmView

suspend fun ViewsForTesting.clickFinishTurn(playerCallToActionView: PlayerCallToActionView) = clickFinishTurn(playerCallToActionView.callToAction as FinishTurnView)

suspend fun ViewsForTesting.clickCancel(playerCallToActionView: PlayerCallToActionView) = clickCancel(playerCallToActionView.callToAction as CancelAndConfirmView)

suspend fun ViewsForTesting.clickConfirm(playerCallToActionView: PlayerCallToActionView) = clickConfirm(playerCallToActionView.callToAction as CancelAndConfirmView)
