package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.FileFilter
import korlibs.render.GameWindow
import korlibs.render.openFileDialog

/** Where the player picks a world file from, e.g.: a world exported earlier and saved as a file. */
interface WorldFilePicker {
    /**
     * The text of the file picked, or `null` when the player picks none. It throws when the file can not be read. In
     * the browser, it never returns when the player cancels.
     */
    suspend fun pickWorld(): String?
}

/** The file picker of the device the game runs on: the system file dialog, or the file picker of the browser. */
class GameWindowWorldFilePicker(
    private val gameWindow: GameWindow,
    messages: Messages,
) : WorldFilePicker {
    // Only the system file dialog filters the files: the browser offers them all.
    internal val worldFiles = FileFilter(messages[UiMessage.WORLD_FILE_PICKER_WORLDS] to listOf("*.toml"))

    override suspend fun pickWorld(): String? = gameWindow.openFileDialog(worldFiles).firstOrNull()?.readString()
}
