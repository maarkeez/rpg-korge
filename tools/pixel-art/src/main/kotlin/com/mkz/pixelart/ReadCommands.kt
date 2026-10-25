package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

class PaletteCommand : CliktCommand("palette") {
    override fun help(context: Context) = "Lists the character of every FamiCube colour, with its hex."

    override fun run() {
        echo("${Palette.TRANSPARENT_CHAR} transparent")
        (1 until Palette.size).forEach { echo("${Palette.char(it)} ${Palette.describe(it)}") }
        echo("${Palette.KEEP_CHAR} keeps a pixel as it is, when painting a grid")
    }
}

class NewCommand : CliktCommand("new") {
    override fun help(context: Context) = "Creates a sprite with empty layers."

    private val file by argument("FILE", help = "The .aseprite file to create.").file()
    private val size by option("--size", help = "WIDTHxHEIGHT, e.g. 16x16.").convert { Size.parse(it) }.required()
    private val layers by option("--layer", help = "A layer, from the top one. Repeat it for several layers (default: base).").multiple(listOf("base"))
    private val force by option("--force", help = "Overwrite the file when it exists.").flag()
    private val noExport by option("--no-export", help = "Do not export the PNG of a sprite under art/ to resources/.").flag()

    override fun run() {
        if (file.exists() && !force) throw PixelArtException("$file already exists. Use --force to overwrite it.")
        if (size.width > Sprite.MAX_SIZE || size.height > Sprite.MAX_SIZE) throw PixelArtException("A sprite can not be larger than ${Sprite.MAX_SIZE}x${Sprite.MAX_SIZE}.")
        val sprite = Sprite(size.width, size.height)
        layers.reversed().forEach { sprite.layers += sprite.newLayer(it) }
        val exported = SpriteFiles.save(sprite, file, export = !noExport)
        echo("Created $file: ${size.width}x${size.height}, layers ${layers.joinToString()}" + (exported?.let { ", exported $it" } ?: ""))
    }
}

class InfoCommand : CliktCommand("info") {
    override fun help(context: Context) = "Prints the size of a sprite, its layers from the top one, and the colours it displays."

    private val file by argument("FILE", help = "The .aseprite file.").file()

    override fun run() {
        val sprite = SpriteFiles.readSprite(file)
        echo("$file: ${sprite.width}x${sprite.height}, ${sprite.layers.size} layer${if (sprite.layers.size == 1) "" else "s"} (top first)")
        val nameWidth = sprite.layers.maxOfOrNull { it.name.length } ?: 0
        sprite.layers.reversed().forEach { layer ->
            val state = if (layer.visible) "visible" else "hidden"
            val opacity = if (layer.opacity == 255) "" else ", opacity ${layer.opacity}"
            val opaque =
                layer.pixels
                    .colourCounts()
                    .values
                    .sum()
            echo("  ${layer.name.padEnd(nameWidth)}  $state$opacity, $opaque opaque px")
        }
        echo("composite:")
        val counts = sprite.composite().colourCounts()
        if (counts.isEmpty()) echo("  nothing is displayed")
        counts.forEach { (index, count) -> echo("  ${Palette.char(index)} ${Palette.describe(index)} ($count px)") }
    }
}

class GridCommand : CliktCommand("grid") {
    override fun help(context: Context) =
        """
        Prints a layer, or the composite, as a grid of colour characters, with the x coordinates above it, the y coordinates on its left and the legend of its colours below it.

        With --plain, prints the grid alone, as 'pixel paint' reads it.
        """.trimIndent()

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val layer by option("--layer", "-l", help = "The layer (default: the composite of the visible layers).")
    private val region by option("--region", help = "Only this region, x,y,WIDTHxHEIGHT.").convert { Region.parse(it) }
    private val plain by option("--plain", help = "Only the grid, without coordinates nor legend.").flag()

    override fun run() {
        val sprite = SpriteFiles.readSprite(file)
        val pixels = sprite.pixels(layer)
        val region = region ?: Region.whole(pixels)
        region.requireInside(pixels)
        val title = "$file, ${layer?.let { "layer '$it'" } ?: "composite"}, region $region"
        echo(if (plain) Grid.plain(pixels, region) else Grid.annotated(pixels, region, title))
    }
}

class GetCommand : CliktCommand("get") {
    override fun help(context: Context) = "Prints the colour of one pixel, of a layer or of the composite. For the composite, also names the layer it comes from."

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val point by argument("X,Y", help = "The pixel.").convert { Point.parse(it) }
    private val layer by option("--layer", "-l", help = "The layer (default: the composite of the visible layers).")

    override fun run() {
        val sprite = SpriteFiles.readSprite(file)
        val pixels = sprite.pixels(layer)
        if (!pixels.contains(point.x, point.y)) throw PixelArtException("${point.x},${point.y} is outside of the ${sprite.width}x${sprite.height} sprite.")
        val index = pixels[point.x, point.y]
        val source = layer ?: sprite.layers.lastOrNull { it.displayed && it.pixels[point.x, point.y] != Palette.TRANSPARENT }?.name
        echo("${point.x},${point.y} ${Palette.char(index)} ${Palette.describe(index)}" + (source?.let { " (layer '$it')" } ?: ""))
    }
}
