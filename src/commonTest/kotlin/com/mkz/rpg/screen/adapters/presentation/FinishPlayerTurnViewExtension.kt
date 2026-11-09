package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.tests.ViewsForTesting

// Page object of [FinishPlayerTurnView].

fun FinishPlayerTurnView.buttonText(): String = button.text

/** Whether the button can be clicked. */
fun FinishPlayerTurnView.isButtonEnabled(): Boolean = button.enabled

/** Whether the button is displayed faded, as a disabled one. */
fun FinishPlayerTurnView.isButtonFaded(): Boolean = button.isFaded()

suspend fun ViewsForTesting.clickFinishPlayerTurn(finishPlayerTurnView: FinishPlayerTurnView) = clickOn(finishPlayerTurnView.button)
