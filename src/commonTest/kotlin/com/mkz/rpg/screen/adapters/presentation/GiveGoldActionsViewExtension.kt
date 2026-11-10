package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.ui.UIButton

fun GiveGoldActionsView.giveLabel(): String = giveButton.text

fun GiveGoldActionsView.isAllEnabled(): Boolean = allButton.enabled

fun GiveGoldActionsView.isAllFaded(): Boolean = allButton.isFaded()

fun GiveGoldActionsView.isAsTallAsFinishPlayerTurn(): Boolean = listOf(cancelButton, allButton, giveButton).all { button -> button.height == UIButton.DEFAULT_SIZE.height }
