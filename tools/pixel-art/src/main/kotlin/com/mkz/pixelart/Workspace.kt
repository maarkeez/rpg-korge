package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

/** What a command changed: the layer, if a single one, and a line describing it. */
class Change(
    val description: String,
    val layerName: String? = null,
)

/** Where the commands that change a sprite read it and save it. */
interface Workspace {
    fun edit(
        file: File,
        command: EditCommand,
        change: (Sprite) -> Change,
    )

    /** The grid to paint: the given file, or the standard input when `null` or `-`. */
    fun readGrid(file: File?): String = if (file == null || file.path == "-") System.`in`.bufferedReader().readText() else file.readText()
}

/** Reads the sprite from its file and saves it back after every command, exporting its PNG. */
class FileWorkspace : Workspace {
    override fun edit(
        file: File,
        command: EditCommand,
        change: (Sprite) -> Change,
    ) {
        val sprite = SpriteFiles.readSprite(file)
        val result = change(sprite)
        save(sprite, file, listOf(result), export = !command.noExport, echo = command::echo)
    }

    companion object {
        fun save(
            sprite: Sprite,
            file: File,
            changes: List<Change>,
            export: Boolean,
            echo: (String) -> Unit,
        ) {
            changes.forEach { echo(it.description) }
            val exported = SpriteFiles.save(sprite, file, export)
            echo("Saved $file" + (exported?.let { ", exported $it" } ?: ""))
        }
    }
}

/** Runs the commands of a `pixel apply` script on one sprite in memory, which is saved once they all succeed. */
class ScriptWorkspace(
    val sprite: Sprite,
) : Workspace {
    val changes = mutableListOf<Change>()

    /** The grid written below the `paint` command being run. */
    var grid: String? = null

    override fun edit(
        file: File,
        command: EditCommand,
        change: (Sprite) -> Change,
    ) {
        changes += change(sprite)
    }

    override fun readGrid(file: File?): String = file?.let { super.readGrid(it) } ?: grid ?: throw PixelArtException("Write the grid to paint below the paint command, ending it with a line 'end'.")
}

/** A command that changes a sprite: it is run on its file, or as a line of a `pixel apply` script. */
abstract class EditCommand(
    name: String,
) : CliktCommand(name) {
    val file by argument("FILE", help = "The .aseprite file.").file()
    val noExport by option("--no-export", help = "Do not export the PNG of a sprite under art/ to resources/.").flag()

    protected val workspace: Workspace get() = currentContext.findObject<Workspace>() ?: FileWorkspace()

    abstract fun change(sprite: Sprite): Change

    override fun run() {
        workspace.edit(file, this, ::change)
    }
}
