package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.GameWindow

actual fun deviceClipboard(gameWindow: GameWindow): Clipboard = GameWindowClipboard(gameWindow)
