package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.tests.ViewsForTesting

// Page object of [FinishTurnView].

fun FinishTurnView.buttonText(): String = button.text

/** Whether the button can be clicked. */
fun FinishTurnView.isButtonEnabled(): Boolean = button.enabled

/** Whether the button is displayed faded, as a disabled one. */
fun FinishTurnView.isButtonFaded(): Boolean = button.isFaded()

suspend fun ViewsForTesting.clickFinishTurn(finishTurnView: FinishTurnView) = clickOn(finishTurnView.button)
