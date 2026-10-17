package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [MapBuilderMenuView].

/** The labels of the options listed, or none while the menu is closed. */
fun MapBuilderMenuView.optionLabels(): List<String> = optionButtons.map { button -> button.text }

suspend fun ViewsForTesting.clickMenuButton(menuView: MapBuilderMenuView) = clickOn(menuView.menuButton)

suspend fun ViewsForTesting.clickMenuOption(
    menuView: MapBuilderMenuView,
    label: String,
) = clickOn(menuView.optionButtons.single { button -> button.text == label })

/** Taps the screen at a point out of the menu, e.g.: on the battlefield. */
suspend fun ViewsForTesting.clickOutOfMenu(
    x: Double,
    y: Double,
) = mouseMoveAndClickTo(x, y)

/** The menu on the screen, or `null` while the map builder scene is not on the stage. */
fun Stage.mapBuilderMenuView(): MapBuilderMenuView? = descendants().filterIsInstance<MapBuilderMenuView>().firstOrNull()
