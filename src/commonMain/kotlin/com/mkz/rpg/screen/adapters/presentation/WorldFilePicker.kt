package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.FileFilter
import korlibs.render.GameWindow
import korlibs.render.openFileDialog

interface WorldFilePicker {
    suspend fun pickWorld(): String?
}

class GameWindowWorldFilePicker(
    private val gameWindow: GameWindow,
    messages: Messages,
) : WorldFilePicker {
    internal val worldFiles = FileFilter(messages[UiMessage.WORLD_FILE_PICKER_WORLDS] to listOf("*.toml"))

    override suspend fun pickWorld(): String? = gameWindow.openFileDialog(worldFiles).firstOrNull()?.readString()
}
