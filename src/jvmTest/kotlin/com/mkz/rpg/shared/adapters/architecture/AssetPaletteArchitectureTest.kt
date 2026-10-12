package com.mkz.rpg.shared.adapters.architecture

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.RGBA
import korlibs.image.format.readBitmap
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import kotlin.math.pow
import kotlin.test.Test

class AssetPaletteArchitectureTest {
    private val paletteFileName = "famicube-palette.png"
    private val imageExtensions = setOf("png", "jpg", "jpeg")

    private val palette: Set<Int> =
        runBlocking {
            resourcesVfs[paletteFileName]
                .readBitmap()
                .pixels()
                .map { it.rgb() }
                .toSet()
        }
    private val assets: Map<String, Bitmap> =
        runBlocking {
            resourcesVfs
                .listRecursiveSimple()
                .filter { file -> file.isImage() && file.fileName() != paletteFileName }
                .associate { file -> file.path.removePrefix("/") to file.readBitmap() }
        }

    @Test
    fun `should only use colours of the palette when is a game asset`() {
        // Given
        val visiblePixelsByAsset = assets.mapValues { (_, bitmap) -> bitmap.pixels().filter { it.a > 0 } }
        // When
        val offPaletteColoursByAsset =
            visiblePixelsByAsset
                .mapValues { (_, pixels) ->
                    pixels
                        .map { it.rgb() }
                        .filterNot { it in palette }
                        .groupingBy { it }
                        .eachCount()
                }.filterValues { it.isNotEmpty() }
        // Then
        palette.shouldHaveSize(64)
        assets.shouldNotBeEmpty()
        val violations =
            offPaletteColoursByAsset.map { (path, offPaletteColours) ->
                val colours =
                    offPaletteColours.entries
                        .sortedByDescending { it.value }
                        .joinToString { (colour, pixels) -> "${colour.hex()} ($pixels px, nearest is ${nearestPaletteColour(colour).hex()})" }
                "Asset '$path' uses colours outside of '$paletteFileName': $colours"
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }

    @Test
    fun `should only use fully transparent or fully opaque pixels when is a game asset`() {
        // Given
        val alphasByAsset = assets.mapValues { (_, bitmap) -> bitmap.pixels().map { it.a } }
        // When
        val semiTransparentPixelsByAsset =
            alphasByAsset
                .mapValues { (_, alphas) -> alphas.count { it in 1..254 } }
                .filterValues { it > 0 }
        // Then
        assets.shouldNotBeEmpty()
        val violations =
            semiTransparentPixelsByAsset.map { (path, semiTransparentPixels) ->
                "Asset '$path' has $semiTransparentPixels semi-transparent pixels. Pixels must be fully transparent or fully opaque."
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }

    private fun VfsFile.fileName(): String = path.substringAfterLast("/")

    private fun VfsFile.isImage(): Boolean = !fileName().startsWith(".") && fileName().substringAfterLast(".").lowercase() in imageExtensions

    private fun Bitmap.pixels(): List<RGBA> = (0 until height).flatMap { y -> (0 until width).map { x -> getRgba(x, y) } }

    private fun RGBA.rgb(): Int = (r shl 16) or (g shl 8) or b

    private fun Int.hex(): String = "#%06x".format(this)

    // Compares colours in the CIELAB colour space, where distances match how different two colours look.
    private fun nearestPaletteColour(colour: Int): Int {
        val lab = colour.toLab()
        return palette.minBy { paletteColour ->
            val paletteLab = paletteColour.toLab()
            lab.indices.sumOf { index -> (lab[index] - paletteLab[index]).pow(2) }
        }
    }

    private fun Int.toLab(): List<Double> {
        fun linear(channel: Int): Double {
            val value = channel / 255.0
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }

        fun f(value: Double): Double = if (value > 0.008856) value.pow(1.0 / 3) else 7.787 * value + 16.0 / 116

        val r = linear((this shr 16) and 0xff)
        val g = linear((this shr 8) and 0xff)
        val b = linear(this and 0xff)
        val x = f((0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047)
        val y = f(0.2126 * r + 0.7152 * g + 0.0722 * b)
        val z = f((0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883)
        return listOf(116 * y - 16, 500 * (x - y), 200 * (y - z))
    }
}
