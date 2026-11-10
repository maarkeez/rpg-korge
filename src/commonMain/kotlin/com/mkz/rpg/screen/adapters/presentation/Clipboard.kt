package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.GameWindow
import korlibs.render.TextClipboardData

interface Clipboard {
    suspend fun copy(text: String)
}

class GameWindowClipboard(
    private val gameWindow: GameWindow,
) : Clipboard {
    override suspend fun copy(text: String) = gameWindow.clipboardWrite(TextClipboardData(text))
}
