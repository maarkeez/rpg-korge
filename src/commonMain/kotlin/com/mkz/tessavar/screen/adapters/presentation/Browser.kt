package com.mkz.tessavar.screen.adapters.presentation

import korlibs.io.net.URL
import korlibs.render.GameWindow
import korlibs.render.browse

interface Browser {
    suspend fun open(url: String)
}

class GameWindowBrowser(
    private val gameWindow: GameWindow,
) : Browser {
    override suspend fun open(url: String) = gameWindow.browse(URL(url))
}
