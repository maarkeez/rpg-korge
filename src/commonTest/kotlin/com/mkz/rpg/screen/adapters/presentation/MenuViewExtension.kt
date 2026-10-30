package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [MenuView] and [MenuScene].

/** The menu on the screen, or `null` while the menu scene is not on the stage. */
fun Stage.menuView(): MenuView? = descendants().filterIsInstance<MenuView>().firstOrNull()

fun MenuView.playNewGameLabel(): String = playNewGameButton.confirmButton.text

fun MenuView.loadGameLabel(): String = loadGameButton.cancelButton.text

fun MenuView.buildMapLabel(): String = buildMapButton.cancelButton.text

suspend fun ViewsForTesting.clickPlayNewGame(menuView: MenuView) = clickOn(menuView.playNewGameButton.confirmButton)

suspend fun ViewsForTesting.clickLoadGame(menuView: MenuView) = clickOn(menuView.loadGameButton.cancelButton)

suspend fun ViewsForTesting.clickBuildMap(menuView: MenuView) = clickOn(menuView.buildMapButton.cancelButton)

/** Whether the options are on the screen, to select one. */
fun MenuView.areOptionsShown(): Boolean = options.isShown()

fun MenuView.languageLabel(): String = languageButton.cancelButton.text

suspend fun ViewsForTesting.clickLanguage(menuView: MenuView) = clickOn(menuView.languageButton.cancelButton)

/** The languages listed, by their names, e.g.: `Español`. */
fun MenuView.languageNames(): List<String> = languageButtons.map { button -> button.cancelButton.text }

/** Whether the languages are on the screen, to pick one. */
fun MenuView.areLanguagesShown(): Boolean = languages.isShown()

suspend fun ViewsForTesting.pickLanguage(
    menuView: MenuView,
    languageName: String,
) = clickOn(menuView.languageButtons.first { button -> button.cancelButton.text == languageName }.cancelButton)
