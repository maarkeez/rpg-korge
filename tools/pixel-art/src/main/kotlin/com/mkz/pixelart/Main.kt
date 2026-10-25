package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import kotlin.system.exitProcess

class Pixel : CliktCommand("pixel") {
    override fun help(context: Context) =
        """
        Draws low resolution pixel art in Aseprite files, for the game's assets.

        Sprites are indexed with the 64 FamiCube colours. Every colour has a fixed character, the same in every file (run 'pixel palette'); '.' is transparent. Grids print one character per pixel.

        Coordinates are x,y from the top-left pixel (0,0). Sizes are WIDTHxHEIGHT, e.g. 16x16. Regions are x,y,WIDTHxHEIGHT, e.g. 2,3,8x4. A COLOUR is its character, its hex (e.g. #151515), or '.' for transparent.

        Layers are listed from the top one. Without --layer, the read commands show the composite: what the visible layers display together, the topmost opaque pixel winning.

        A sprite under an art/ folder is exported to the same path under resources/ every time it is saved, e.g. art/unit/knight.aseprite to resources/unit/knight.png.
        """.trimIndent()

    override fun run() = Unit
}

fun pixel(): Pixel =
    Pixel().subcommands(
        PaletteCommand(),
        NewCommand(),
        InfoCommand(),
        GridCommand(),
        GetCommand(),
        LayerCommand(),
        ImportCommand(),
        ExportCommand(),
    )

fun main(args: Array<String>) {
    try {
        pixel().main(args)
    } catch (exception: PixelArtException) {
        System.err.println("error: ${exception.message}")
        exitProcess(1)
    }
}
