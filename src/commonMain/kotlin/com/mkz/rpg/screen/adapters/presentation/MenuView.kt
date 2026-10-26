package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size

/**
 * The first screen of the game: playing a new game, its primary action, or building a map. The options are hidden as
 * soon as one is selected, as the scene it opens takes a while to load, and so it can not be selected twice.
 */
class MenuView(
    texts: Texts,
    onNewGameSelected: () -> Unit,
    onMapBuilderSelected: () -> Unit,
) : UIContainer(Size(width = BUTTON_WIDTH, height = 2 * BUTTON_HEIGHT + BUTTON_GAP)) {
    companion object {
        const val BUTTON_WIDTH = 250.0
        const val BUTTON_HEIGHT = 32.0
        const val BUTTON_GAP = 15.0
    }

    internal val playNewGameButton =
        ConfirmButton(label = texts[UiText.MENU_PLAY_NEW_GAME], buttonWidth = BUTTON_WIDTH, onConfirmed = { select(onNewGameSelected) })
    internal val buildMapButton =
        CancelButton(label = texts[UiText.MENU_BUILD_MAP], buttonWidth = BUTTON_WIDTH, onCancelled = { select(onMapBuilderSelected) })

    internal val options =
        uiVerticalStack(padding = BUTTON_GAP) {
            addChild(playNewGameButton)
            addChild(buildMapButton)
        }

    private fun select(onSelected: () -> Unit) {
        options.visible = false
        onSelected()
    }
}
