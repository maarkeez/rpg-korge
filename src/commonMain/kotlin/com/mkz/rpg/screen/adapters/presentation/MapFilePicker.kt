package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.FileFilter
import korlibs.render.GameWindow
import korlibs.render.openFileDialog

/** Where the player picks a map file from, e.g.: a map exported earlier and saved as a file. */
interface MapFilePicker {
    /**
     * The text of the file picked, or `null` when the player picks none. It throws when the file can not be read. In
     * the browser, it never returns when the player cancels.
     */
    suspend fun pickMap(): String?
}

/** The file picker of the device the game runs on: the system file dialog, or the file picker of the browser. */
class GameWindowMapFilePicker(
    private val gameWindow: GameWindow,
    texts: Texts,
) : MapFilePicker {
    // Only the system file dialog filters the files: the browser offers them all.
    internal val mapFiles = FileFilter(texts[UiText.MAP_FILE_PICKER_MAPS] to listOf("*.toml"))

    override suspend fun pickMap(): String? = gameWindow.openFileDialog(mapFiles).firstOrNull()?.readString()
}
