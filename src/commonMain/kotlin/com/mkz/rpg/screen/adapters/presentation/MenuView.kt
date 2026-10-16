package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size

/** The first screen of the game: playing a new game, its primary action, or building a map. */
class MenuView(
    onNewGameSelected: () -> Unit,
    onMapBuilderSelected: () -> Unit,
) : UIContainer(Size(width = BUTTON_WIDTH, height = 2 * BUTTON_HEIGHT + BUTTON_GAP)) {
    companion object {
        const val BUTTON_WIDTH = 250.0
        const val BUTTON_HEIGHT = 32.0
        const val BUTTON_GAP = 15.0
    }

    internal val playNewGameButton = ConfirmButton(label = "Play new game", buttonWidth = BUTTON_WIDTH, onConfirmed = onNewGameSelected)
    internal val buildMapButton = CancelButton(label = "Build a map", buttonWidth = BUTTON_WIDTH, onCancelled = onMapBuilderSelected)

    init {
        uiVerticalStack(padding = BUTTON_GAP) {
            addChild(playNewGameButton)
            addChild(buildMapButton)
        }
    }
}
