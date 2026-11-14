package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun WorldBuilderMenuView.optionLabels(): List<String> = optionButtons.map { button -> button.text }

suspend fun ViewsForTesting.clickMenuButton(menuView: WorldBuilderMenuView) = clickOn(menuView.menuButton)

suspend fun ViewsForTesting.clickMenuOption(
    menuView: WorldBuilderMenuView,
    label: String,
) = clickOn(menuView.optionButtons.single { button -> button.text == label })

suspend fun ViewsForTesting.clickOutOfMenu(
    x: Double,
    y: Double,
) = mouseMoveAndClickTo(x, y)

fun Stage.worldBuilderMenuView(): WorldBuilderMenuView? = descendants().filterIsInstance<WorldBuilderMenuView>().firstOrNull()
