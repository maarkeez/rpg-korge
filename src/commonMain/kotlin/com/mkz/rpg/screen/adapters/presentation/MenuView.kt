package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size

/**
 * The first screen of the game: playing a new game, its primary action, loading a saved game, building a world, or picking
 * the language. The options are hidden as soon as one of them is selected, as the scene it opens takes a while to load,
 * and so it can not be selected twice. Load game lists the save slots in their place, until [displayOptions].
 *
 * Language lists the languages in place of the options, each named in its own language. Selecting one translates the
 * game into it, for the rest of the session, and displays the options again, relabelled.
 */
class MenuView(
    private val messages: Messages,
    onNewGameSelected: () -> Unit,
    onLoadGameSelected: () -> Unit,
    onWorldBuilderSelected: () -> Unit,
) : UIContainer(Size(width = BUTTON_WIDTH, height = 4 * BUTTON_HEIGHT + 3 * BUTTON_GAP)) {
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

    internal val options =
        uiVerticalStack(padding = BUTTON_GAP) {
            addChild(playNewGameButton)
            addChild(loadGameButton)
            addChild(buildWorldButton)
            addChild(languageButton)
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

    /** Displays the options again, e.g.: Back from the save slots. */
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
        displayLanguages(isDisplayed = false)
    }
}
