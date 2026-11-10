package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.tests.ViewsForTesting

fun FinishPlayerTurnView.buttonText(): String = button.text

fun FinishPlayerTurnView.isButtonEnabled(): Boolean = button.enabled

fun FinishPlayerTurnView.isButtonFaded(): Boolean = button.isFaded()

suspend fun ViewsForTesting.clickFinishPlayerTurn(finishPlayerTurnView: FinishPlayerTurnView) = clickOn(finishPlayerTurnView.button)
