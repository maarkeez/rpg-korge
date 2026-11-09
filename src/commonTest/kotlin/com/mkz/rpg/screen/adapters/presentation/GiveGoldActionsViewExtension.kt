package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.ui.UIButton

// Page object of [GiveGoldActionsView].

/** The label of Give, with the amount, e.g.: `Give 25 G`. */
fun GiveGoldActionsView.giveLabel(): String = giveButton.text

fun GiveGoldActionsView.isAllEnabled(): Boolean = allButton.enabled

fun GiveGoldActionsView.isAllFaded(): Boolean = allButton.isFaded()

/** Whether every button is as tall as the finish turn, so all the actions of the game are on one level. */
fun GiveGoldActionsView.isAsTallAsFinishPlayerTurn(): Boolean = listOf(cancelButton, allButton, giveButton).all { button -> button.height == UIButton.DEFAULT_SIZE.height }
