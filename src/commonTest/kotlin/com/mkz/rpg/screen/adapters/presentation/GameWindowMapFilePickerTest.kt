package com.mkz.rpg.screen.adapters.presentation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.io.file.VfsFile
import korlibs.io.file.std.MemoryVfsMix
import korlibs.render.DialogInterface
import korlibs.render.FileFilter
import korlibs.render.GameWindow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GameWindowMapFilePickerTest {
    // A game window whose file dialog picks the given files, and records the filters it is opened with.
    private class FakeFileDialogGameWindow(
        private val pickedFiles: List<VfsFile>,
    ) : GameWindow() {
        val filters = mutableListOf<FileFilter?>()

        override val dialogInterface: DialogInterface =
            object : DialogInterface {
                override suspend fun openFileDialog(
                    filter: FileFilter?,
                    write: Boolean,
                    multi: Boolean,
                    currentDir: VfsFile?,
                ): List<VfsFile> {
                    filters += filter
                    return pickedFiles
                }
            }
    }

    private val files = MemoryVfsMix("map.toml" to "tiles = [[\"sand\"]]")

    @Test
    fun `pickMap should return the text of the file picked`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(listOf(files["map.toml"]))
            // When
            val text = GameWindowMapFilePicker(gameWindow).pickMap()
            // Then
            text shouldBe "tiles = [[\"sand\"]]"
            gameWindow.filters.shouldContainExactly(GameWindowMapFilePicker.MAP_FILES)
        }

    @Test
    fun `pickMap should return null when no file is picked`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(emptyList())
            // When
            val text = GameWindowMapFilePicker(gameWindow).pickMap()
            // Then
            text shouldBe null
        }

    @Test
    fun `pickMap should throw when the file picked can not be read`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(listOf(files["missing.toml"]))
            // When
            val picking = suspend { GameWindowMapFilePicker(gameWindow).pickMap() }
            // Then
            shouldThrow<Exception> { picking() }
        }
}
