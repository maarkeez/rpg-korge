package com.mkz.rpg.shared.adapters.architecture

import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.RGBA
import korlibs.image.format.readBitmap
import korlibs.io.file.VfsFile
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AssetOutlineArchitectureTest {
    private val outlineColour = 0x151515
    private val darkLuminance = 60.0
    private val outlinedFolders = listOf("unit/", "npc/", "lootableItem/", "building/")
    private val outlinedFiles = setOf("battlefield/chest.png")

    // Drawn without a single outline colour: its hair, black and dark green all reach its silhouette.
    private val notOutlinedFiles = setOf("unit/rat_portrait.png")

    private val assets: Map<String, Bitmap> =
        runBlocking {
            ResourcesBundle.root
                .listRecursiveSimple()
                .filter { file -> file.isImage() }
                .map { file -> file.path.removePrefix("/") to file }
                .filter { (path, _) -> (outlinedFolders.any { path.startsWith(it) } || path in outlinedFiles) && path !in notOutlinedFiles }
                .associate { (path, file) -> path to file.readBitmap() }
        }

    @Test
    fun `should outline with #151515 when is a game asset`() {
        // Given
        val silhouettesByAsset = assets.mapValues { (_, bitmap) -> bitmap.silhouette() }
        // When
        val otherDarkOutlineColoursByAsset =
            silhouettesByAsset
                .mapValues { (_, silhouette) ->
                    silhouette
                        .filter { it.luminance() < darkLuminance }
                        .map { it.rgb() }
                        .filterNot { it == outlineColour }
                        .groupingBy { it }
                        .eachCount()
                }.filterValues { it.isNotEmpty() }
        // Then
        assets.shouldNotBeEmpty()
        val violations =
            otherDarkOutlineColoursByAsset.map { (path, colours) ->
                val outline = colours.entries.sortedByDescending { it.value }.joinToString { (colour, pixels) -> "${colour.hex()} ($pixels px)" }
                "Asset '$path' is outlined with $outline instead of ${outlineColour.hex()}"
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }

    private fun VfsFile.isImage(): Boolean = path.substringAfterLast("/").let { name -> !name.startsWith(".") && name.endsWith(".png") }

    // The opaque pixels next to a transparent pixel or to the edge of the image, diagonals included.
    private fun Bitmap.silhouette(): List<RGBA> =
        (0 until height).flatMap { y ->
            (0 until width)
                .filter { x -> getRgba(x, y).a > 0 }
                .filter { x ->
                    (-1..1).any { dy ->
                        (-1..1).any { dx -> x + dx !in 0 until width || y + dy !in 0 until height || getRgba(x + dx, y + dy).a == 0 }
                    }
                }.map { x -> getRgba(x, y) }
        }

    private fun RGBA.luminance(): Double = 0.299 * r + 0.587 * g + 0.114 * b

    private fun RGBA.rgb(): Int = (r shl 16) or (g shl 8) or b

    private fun Int.hex(): String = "#%06x".format(this)
}
