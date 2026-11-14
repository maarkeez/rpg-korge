package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size

class MenuView(
    private val messages: Messages,
    onNewGameSelected: () -> Unit,
    onLoadGameSelected: () -> Unit,
    onWorldBuilderSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
    onWishlistSelected: () -> Unit,
) : UIContainer(Size(width = BUTTON_WIDTH, height = 6 * BUTTON_HEIGHT + 5 * BUTTON_GAP)) {
    companion object {
        const val BUTTON_WIDTH = 250.0
        const val BUTTON_HEIGHT = 32.0
        const val BUTTON_GAP = 15.0
    }

    internal val playNewGameButton =
        ConfirmButton(label = messages[UiMessage.MENU_PLAY_NEW_GAME], buttonWidth = BUTTON_WIDTH, onConfirmed = { select(onNewGameSelected) })
    internal val loadGameButton =
        CancelButton(label = messages[UiMessage.MENU_LOAD_GAME], buttonWidth = BUTTON_WIDTH, onCancelled = { select(onLoadGameSelected) })
    internal val buildWorldButton =
        CancelButton(label = messages[UiMessage.MENU_BUILD_WORLD], buttonWidth = BUTTON_WIDTH, onCancelled = { select(onWorldBuilderSelected) })
    internal val languageButton =
        CancelButton(label = messages[UiMessage.MENU_LANGUAGE], buttonWidth = BUTTON_WIDTH, onCancelled = { displayLanguages(isDisplayed = true) })
    internal val privacyButton =
        CancelButton(label = messages[UiMessage.MENU_PRIVACY], buttonWidth = BUTTON_WIDTH, onCancelled = onPrivacySelected)
    internal val wishlistButton =
        ConfirmButton(
            label = messages[UiMessage.MENU_WISHLIST],
            buttonWidth = BUTTON_WIDTH,
            color = HudStyle.COIN_MIDDLE,
            pressedColor = HudStyle.COIN_PRESSED,
            textColor = HudStyle.PANEL,
            onConfirmed = onWishlistSelected,
        )

    internal val options =
        uiVerticalStack(padding = BUTTON_GAP) {
            addChild(playNewGameButton)
            addChild(loadGameButton)
            addChild(buildWorldButton)
            addChild(languageButton)
            addChild(privacyButton)
            addChild(wishlistButton)
        }

    internal val languageButtons =
        messages.languages().map { language ->
            CancelButton(label = language.name, buttonWidth = BUTTON_WIDTH, onCancelled = { selectLanguage(language.code) })
        }

    internal val languages =
        uiVerticalStack(padding = BUTTON_GAP) {
            visible = false
            languageButtons.forEach(::addChild)
        }

    fun displayOptions() = displayLanguages(isDisplayed = false)

    private fun select(onSelected: () -> Unit) {
        options.visible = false
        onSelected()
    }

    private fun displayLanguages(isDisplayed: Boolean) {
        options.visible = !isDisplayed
        languages.visible = isDisplayed
    }

    private fun selectLanguage(languageCode: String) {
        messages.selectLanguage(languageCode)
        playNewGameButton.confirmButton.text = messages[UiMessage.MENU_PLAY_NEW_GAME]
        loadGameButton.cancelButton.text = messages[UiMessage.MENU_LOAD_GAME]
        buildWorldButton.cancelButton.text = messages[UiMessage.MENU_BUILD_WORLD]
        languageButton.cancelButton.text = messages[UiMessage.MENU_LANGUAGE]
        privacyButton.cancelButton.text = messages[UiMessage.MENU_PRIVACY]
        wishlistButton.confirmButton.text = messages[UiMessage.MENU_WISHLIST]
        displayLanguages(isDisplayed = false)
    }
}
