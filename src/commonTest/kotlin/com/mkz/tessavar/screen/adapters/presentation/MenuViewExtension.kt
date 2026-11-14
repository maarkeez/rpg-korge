package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun Stage.menuView(): MenuView? = descendants().filterIsInstance<MenuView>().firstOrNull()

fun MenuView.playNewGameLabel(): String = playNewGameButton.confirmButton.text

fun MenuView.loadGameLabel(): String = loadGameButton.cancelButton.text

fun MenuView.buildWorldLabel(): String = buildWorldButton.cancelButton.text

suspend fun ViewsForTesting.clickPlayNewGame(menuView: MenuView) = clickOn(menuView.playNewGameButton.confirmButton)

suspend fun ViewsForTesting.clickLoadGame(menuView: MenuView) = clickOn(menuView.loadGameButton.cancelButton)

suspend fun ViewsForTesting.clickBuildWorld(menuView: MenuView) = clickOn(menuView.buildWorldButton.cancelButton)

fun MenuView.areOptionsShown(): Boolean = options.isShown()

fun MenuView.languageLabel(): String = languageButton.cancelButton.text

suspend fun ViewsForTesting.clickLanguage(menuView: MenuView) = clickOn(menuView.languageButton.cancelButton)

fun MenuView.languageNames(): List<String> = languageButtons.map { button -> button.cancelButton.text }

fun MenuView.areLanguagesShown(): Boolean = languages.isShown()

suspend fun ViewsForTesting.pickLanguage(
    menuView: MenuView,
    languageName: String,
) = clickOn(menuView.languageButtons.first { button -> button.cancelButton.text == languageName }.cancelButton)

fun MenuView.privacyLabel(): String = privacyButton.cancelButton.text

suspend fun ViewsForTesting.clickPrivacy(menuView: MenuView) = clickOn(menuView.privacyButton.cancelButton)

fun MenuView.wishlistLabel(): String = wishlistButton.confirmButton.text

suspend fun ViewsForTesting.clickWishlist(menuView: MenuView) = clickOn(menuView.wishlistButton.confirmButton)
