package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.GameWindow
import korlibs.render.TextClipboardData

/** Where the player copies text to, to paste it somewhere else, e.g.: an exported map. */
interface Clipboard {
    suspend fun copy(text: String)
}

/** The clipboard of the device the game runs on. */
class GameWindowClipboard(
    private val gameWindow: GameWindow,
) : Clipboard {
    override suspend fun copy(text: String) = gameWindow.clipboardWrite(TextClipboardData(text))
}
