package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [FinishTurnView].

fun FinishTurnView.buttonText(): String = button.text

/** Whether the button can be clicked. A disabled button is displayed greyed out. */
fun FinishTurnView.isButtonEnabled(): Boolean = button.enabled

suspend fun ViewsForTesting.clickFinishTurn(finishTurnView: FinishTurnView) = clickOn(finishTurnView.button)
