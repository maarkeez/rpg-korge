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
        save(sprite, file, command, listOf(result))
    }

    companion object {
        fun save(
            sprite: Sprite,
            file: File,
            command: EditCommand,
            changes: List<Change>,
        ) {
            changes.forEach { command.echo(it.description) }
            val exported = SpriteFiles.save(sprite, file, export = !command.noExport)
            command.echo("Saved $file" + (exported?.let { ", exported $it" } ?: ""))
        }
    }
}

/** A command that changes a sprite: it is run on its file, or as a line of a `pixel apply` script. */
abstract class EditCommand(
    name: String,
) : CliktCommand(name) {
    val file by argument("FILE", help = "The .aseprite file.").file()
    val noExport by option("--no-export", help = "Do not export the PNG of a sprite under art/ to resources/.").flag()

    abstract fun change(sprite: Sprite): Change

    override fun run() {
        val workspace = currentContext.findObject<Workspace>() ?: FileWorkspace()
        workspace.edit(file, this, ::change)
    }
}
