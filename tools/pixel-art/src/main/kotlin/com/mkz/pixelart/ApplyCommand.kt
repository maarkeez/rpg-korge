package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file

class ApplyCommand : CliktCommand("apply") {
    override fun help(context: Context) =
        """
        Runs a script of commands on a sprite, and saves it once, only when all of them succeed.

        Each line is a drawing command (set, paint, fill, rect, line, replace, flip, mirror, shift, copy) or a layer command (layer add, remove, rename, move, hide, show), without 'pixel' nor FILE. A paint command is followed by its grid, ending with a line 'end'. Lines starting with '#' and blank lines are ignored. E.g.:

        ```
        pixel apply art/unit/goblin.aseprite <<'SCRIPT'
        layer add outline
        rect --layer base 4,4,8x8 a --filled
        paint --layer outline --at 4,3
        -WWWWWW-
        end
        mirror --layer outline --from left
        SCRIPT
        ```
        """.trimIndent()

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val script by argument("SCRIPT", help = "The file holding the script (default: the standard input).").file().optional()
    private val noExport by option("--no-export", help = "Do not export the PNG of a sprite under art/ to resources/.").flag()
    private val quiet by option("--quiet", "-q", help = "Do not print the layers drawn on after the changes.").flag()

    override fun run() {
        val text = if (script == null || script!!.path == "-") System.`in`.bufferedReader().readText() else script!!.readText()
        val workspace = ScriptWorkspace(SpriteFiles.readSprite(file))
        val lines = text.lines()
        var number = 0
        while (number < lines.size) {
            val line = lines[number++].trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            val lineNumber = number
            val tokens = tokenize(line, lineNumber)
            workspace.grid =
                if (tokens.first() == "paint") {
                    val end =
                        (number until lines.size).firstOrNull { lines[it].trim() == "end" }
                            ?: throw PixelArtException("Line $lineNumber: the grid of 'paint' does not end with a line 'end'. Nothing was saved.")
                    lines.subList(number, end).joinToString("\n").also { number = end + 1 }
                } else {
                    null
                }
            run(tokens, workspace, lineNumber, line)
        }
        FileWorkspace.save(workspace.sprite, file, workspace.changes, export = !noExport, quiet = quiet, echo = ::echo)
    }

    private fun run(
        tokens: List<String>,
        workspace: ScriptWorkspace,
        lineNumber: Int,
        line: String,
    ) {
        // The file is inserted where each command expects it: right after the command, or after the layer command.
        val argv =
            if (tokens.first() == "layer" && tokens.size > 1) {
                listOf("layer", tokens[1], file.path) + tokens.drop(2)
            } else {
                listOf(tokens.first(), file.path) + tokens.drop(1)
            }
        val commands = ScriptCommands().context { obj = workspace }
        try {
            commands.parse(argv)
        } catch (exception: PixelArtException) {
            throw PixelArtException("Line $lineNumber '$line': ${exception.message} Nothing was saved.")
        } catch (exception: CliktError) {
            val message = commands.getFormattedHelp(exception) ?: exception.message ?: exception.toString()
            throw PixelArtException("Line $lineNumber '$line': ${message.trim()}\nNothing was saved.")
        }
    }

    /** Splits a line on whitespace, keeping what is quoted together. */
    private fun tokenize(
        line: String,
        lineNumber: Int,
    ): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var quote: Char? = null
        var inToken = false
        for (char in line) {
            when {
                quote != null && char == quote -> quote = null
                quote != null -> current.append(char)
                char == '\'' || char == '"' -> {
                    quote = char
                    inToken = true
                }
                char.isWhitespace() -> {
                    if (inToken) tokens += current.toString()
                    current.clear()
                    inToken = false
                }
                else -> {
                    current.append(char)
                    inToken = true
                }
            }
        }
        if (quote != null) throw PixelArtException("Line $lineNumber: a quote is not closed. Nothing was saved.")
        if (inToken) tokens += current.toString()
        return tokens
    }
}

/** The commands a script can run. */
private class ScriptCommands : CliktCommand("pixel") {
    override fun run() = Unit

    init {
        subcommands(drawCommands() + LayerCommand())
    }
}
