package com.mkz.tessavar.screen.adapters.presentation

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

class GameWindowWorldFilePickerTest {
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

    private val files = MemoryVfsMix("world.toml" to "tiles = [[\"sand\"]]")

    @Test
    fun `pickWorld should return the text of the file picked`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(listOf(files["world.toml"]))
            // When
            val picker = GameWindowWorldFilePicker(gameWindow, MessagesMother.messages())
            val text = picker.pickWorld()
            // Then
            text shouldBe "tiles = [[\"sand\"]]"
            gameWindow.filters.shouldContainExactly(picker.worldFiles)
        }

    @Test
    fun `pickWorld should return null when no file is picked`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(emptyList())
            // When
            val text = GameWindowWorldFilePicker(gameWindow, MessagesMother.messages()).pickWorld()
            // Then
            text shouldBe null
        }

    @Test
    fun `pickWorld should throw when the file picked can not be read`() =
        runTest {
            // Given
            val gameWindow = FakeFileDialogGameWindow(listOf(files["missing.toml"]))
            // When
            val picking = suspend { GameWindowWorldFilePicker(gameWindow, MessagesMother.messages()).pickWorld() }
            // Then
            shouldThrow<Exception> { picking() }
        }
}
