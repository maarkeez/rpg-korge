package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.io.File
import java.util.concurrent.TimeUnit

class PreviewCommand : CliktCommand("preview") {
    override fun help(context: Context) =
        """
        Writes an upscaled PNG of a sprite, or of one of its layers, to look at.

        Every pixel is a square, transparent pixels show a grey checkerboard, thin lines separate the pixels, magenta lines the 16x16 tiles, and the x and y coordinates are written above and on the left. Prints the path of the PNG.
        """.trimIndent()

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val layer by option("--layer", "-l", help = "Only this layer, even when hidden (default: the composite of the visible layers).")
    private val out by option("--out", "-o", help = "The PNG to write (default: a file in the temporary folder).").file(canBeDir = false)
    private val scale by option("--scale", help = "The size of every pixel (default: about 512 px for the longest side).").int().restrictTo(1..64)
    private val noGrid by option("--no-grid", help = "Without the lines between the pixels and the tiles.").flag()
    private val tile by option("--tile", help = "The size of the tiles, 0 for none (default: 16, the tiles of the battlefield).").int().default(16)

    override fun run() {
        val sprite = SpriteFiles.readSprite(file)
        val pixels = sprite.pixels(layer)
        val png = out ?: File(System.getProperty("java.io.tmpdir"), "pixel-preview/${file.nameWithoutExtension}${layer?.let { "-$it" } ?: ""}.png")
        Png.writeImage(Preview.render(pixels, scale ?: Preview.defaultScale(pixels), grid = !noGrid, tile = tile), png)
        echo(png.absolutePath)
    }
}

class DiffCommand : CliktCommand("diff") {
    override fun help(context: Context) =
        """
        Shows the pixels changed since a git revision of the sprite, or compared with another sprite.

        Lists every layer added, removed or changed, then prints the changed pixels of the composite (or of --layer) before and after, '-' marking the pixels that did not change. Only the region holding the changes is printed.
        """.trimIndent()

    private val file by argument("FILE", help = "The .aseprite file.").file()
    private val against by option("--against", help = "The git revision to compare with (default: HEAD).").default("HEAD")
    private val with by option("--with", help = "Compare with this .aseprite file instead of a git revision.").file()
    private val layer by option("--layer", "-l", help = "Only this layer (default: the composite of the visible layers).")

    override fun run() {
        val after = SpriteFiles.readSprite(file)
        val (before, beforeName) = with?.let { SpriteFiles.readSprite(it) to it.path } ?: (readRevision() to "$against:$file")
        echo("$file compared with $beforeName")
        if (before.width != after.width || before.height != after.height) {
            echo("The size changed from ${before.width}x${before.height} to ${after.width}x${after.height}.")
            return
        }
        if (layer == null) layerChanges(before, after).forEach { echo("  $it") }

        val beforePixels = layer?.let { before.layerOrNull(it)?.pixels ?: Pixels(before.width, before.height) } ?: before.composite()
        val afterPixels = after.pixels(layer)
        val changed =
            Region
                .whole(afterPixels)
                .points()
                .filter { beforePixels[it.x, it.y] != afterPixels[it.x, it.y] }
                .toSet()
        val subject = layer?.let { "layer '$it'" } ?: "composite"
        if (changed.isEmpty()) {
            echo("No pixel of the $subject changed.")
            return
        }
        val region = Region(changed.minOf { it.x }, changed.minOf { it.y }, changed.maxOf { it.x } - changed.minOf { it.x } + 1, changed.maxOf { it.y } - changed.minOf { it.y } + 1)
        echo("${changed.size} px of the $subject changed, in region $region.")
        echo("")
        echo(Grid.annotated(beforePixels, region, "before:") { it in changed })
        echo("")
        echo(Grid.annotated(afterPixels, region, "after:") { it in changed })
    }

    private fun layerChanges(
        before: Sprite,
        after: Sprite,
    ): List<String> {
        val names = (before.layerNamesFromTop() + after.layerNamesFromTop()).distinct()
        return names.mapNotNull { name ->
            val old = before.layerOrNull(name)
            val new = after.layerOrNull(name)
            when {
                old == null -> "layer '$name': added"
                new == null -> "layer '$name': removed"
                else -> {
                    val pixels = Region.whole(new.pixels).points().count { old.pixels[it.x, it.y] != new.pixels[it.x, it.y] }
                    listOfNotNull(
                        pixels.takeIf { it > 0 }?.let { "$it px changed" },
                        if (old.visible != new.visible) (if (new.visible) "shown" else "hidden") else null,
                        if (old.opacity != new.opacity) "opacity ${old.opacity} to ${new.opacity}" else null,
                    ).takeIf { it.isNotEmpty() }?.let { "layer '$name': ${it.joinToString()}" }
                }
            }
        }
    }

    private fun readRevision(): Sprite {
        val absolute = file.absoluteFile
        val process =
            ProcessBuilder("git", "-C", absolute.parentFile.path, "show", "$against:./${absolute.name}")
                .redirectErrorStream(false)
                .start()
        val bytes = process.inputStream.readBytes()
        val error = process.errorStream.bufferedReader().readText()
        process.waitFor(30, TimeUnit.SECONDS)
        if (process.exitValue() != 0) throw PixelArtException("Could not read $file at $against: ${error.trim()}")
        return Aseprite.read(bytes, "$against:$file")
    }
}
