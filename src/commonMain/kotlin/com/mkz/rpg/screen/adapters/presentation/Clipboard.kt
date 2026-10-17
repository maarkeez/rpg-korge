package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.GameWindow
import korlibs.render.TextClipboardData

/** Where the player copies text to, to paste it somewhere else, e.g.: an exported map. */
interface Clipboard {
    suspend fun copy(text: String)

    /**
     * The text copied, or `null` when there is none or it can not be read (e.g.: the browser is not allowed to). The
     * coroutine is cancelled when the player cancels pasting.
     */
    suspend fun paste(): String?
}

/** The clipboard of the device the game runs on, with the means of the platform to paste, e.g.: a paste box in the browser. */
expect fun deviceClipboard(gameWindow: GameWindow): Clipboard

/** The clipboard of the device the game runs on. */
class GameWindowClipboard(
    private val gameWindow: GameWindow,
) : Clipboard {
    override suspend fun copy(text: String) = gameWindow.clipboardWrite(TextClipboardData(text))

    // Reading fails, e.g.: when the browser is denied the clipboard or it holds an image.
    override suspend fun paste(): String? =
        try {
            (gameWindow.clipboardRead() as? TextClipboardData)?.text
        } catch (_: Exception) {
            null
        }
}
