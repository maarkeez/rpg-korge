package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int

class ImportCommand : CliktCommand("import") {
    override fun help(context: Context) =
        """
        Imports a PNG into a layer of a sprite.

        When the sprite does not exist, it is created with the size of the PNG. Otherwise, the PNG must have its size: an existing layer is replaced, keeping its place, and a new one is added at the top.

        The PNG must only use opaque FamiCube colours and fully transparent pixels. Otherwise, the import fails listing each other colour with its closest FamiCube colour, unless --snap replaces them.
        """.trimIndent()

    private val png by argument("PNG", help = "The PNG to import.").file(mustExist = true, canBeDir = false)
    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val layerName by option("--layer", "-l", help = "The layer to import into (default: base).").default("base")
    private val snap by option("--snap", help = "Replace each off-palette colour by its closest FamiCube colour, and each semi-transparent pixel by transparent or opaque.").flag()
    private val alphaThreshold by option(
        "--alpha-threshold",
        help = "With --snap, the alpha from which a pixel is opaque (default: ${Png.DEFAULT_ALPHA_THRESHOLD}).",
    ).int().default(Png.DEFAULT_ALPHA_THRESHOLD)
    private val noExport by option("--no-export", help = "Do not export the PNG of a sprite under art/ to resources/.").flag()

    override fun run() {
        val imported = Png.read(png, snap, alphaThreshold)
        val pixels = imported.pixels
        val sprite = if (file.exists()) SpriteFiles.readSprite(file) else Sprite(pixels.width, pixels.height)
        if (sprite.width != pixels.width || sprite.height != pixels.height) {
            throw PixelArtException("$png is ${pixels.width}x${pixels.height}, but $file is ${sprite.width}x${sprite.height}.")
        }
        val existing = sprite.layerOrNull(layerName)
        val layer = Layer(layerName, pixels, visible = existing?.visible ?: true, opacity = existing?.opacity ?: 255)
        if (existing == null) {
            sprite.validateLayerName(layerName)
            sprite.layers += layer
        } else {
            sprite.layers[sprite.layers.indexOf(existing)] = layer
        }

        imported.snapped.forEach { (rgb, snapped) ->
            echo("Snapped ${Palette.hex(rgb)} (${snapped.second} px) to ${Palette.describe(snapped.first)} '${Palette.char(snapped.first)}'")
        }
        if (snap && imported.semiTransparent > 0) echo("Snapped ${imported.semiTransparent} semi-transparent px, with alpha threshold $alphaThreshold")
        val exported = SpriteFiles.save(sprite, file, export = !noExport)
        echo("Imported $png into layer '$layerName' of $file" + (exported?.let { ", exported $it" } ?: ""))
    }
}

class ExportCommand : CliktCommand("export") {
    override fun help(context: Context) =
        """
        Exports the composite of a sprite, or one of its layers, to PNG.

        A sprite under art/ is exported to the same path under resources/ unless told otherwise. It is also exported every time it is saved, so this is only needed to export elsewhere or a single layer.
        """.trimIndent()

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val out by option("--out", "-o", help = "The PNG to write (default: the same path under resources/, for a sprite under art/).").file(canBeDir = false)
    private val layer by option("--layer", "-l", help = "Only this layer, even when hidden (default: the composite of the visible layers).")

    override fun run() {
        val sprite = SpriteFiles.readSprite(file)
        val png = out ?: SpriteFiles.exportedPng(file) ?: throw PixelArtException("$file is not under an art/ folder: tell where to export it with --out.")
        Png.write(sprite.pixels(layer), png)
        echo("Exported ${layer?.let { "layer '$it' of " } ?: ""}$file to $png")
    }
}
