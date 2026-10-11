package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [FinishTurnView].

fun FinishTurnView.buttonText(): String = button.text

suspend fun ViewsForTesting.clickFinishTurn(finishTurnView: FinishTurnView) = clickOn(finishTurnView.button)
