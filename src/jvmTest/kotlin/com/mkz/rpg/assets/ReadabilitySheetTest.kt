package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import korlibs.image.format.ImageEncodingProps
import korlibs.image.format.PNG
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

// Runs with `./gradlew generateContactSheet`, next to AssetContactSheetTest.
@Tag("contact-sheet")
class ReadabilitySheetTest {
    @Test
    fun `should draw every unit and highlight over every base terrain tile when the readability sheet is written`() {
        // Given
        val file = File(READABILITY_SHEET_PATH)
        // When
        runBlocking { writeReadabilitySheet() }
        // Then
        assertThat(file).exists()
        assertThat(file.length()).isGreaterThan(0)
    }
}

private const val READABILITY_SHEET_PATH: String = "build/readability-sheet.png"
private const val SCALE: Int = 3
private const val CELL_ART_SIZE: Int = 2 * TILE_PIXEL_SIZE
private const val CELL_SIZE: Int = CELL_ART_SIZE * SCALE
private const val CELL_GAP: Int = 4
private const val PANEL_PADDING: Int = 8
private const val PANEL_GAP: Int = 16
private val SHEET_BACKGROUND: RGBA = RGBA(0xFF211640.toInt())

private val READABILITY_UNIT_PATHS = listOf("unit/knight.png", "unit/rat.png", "unit/bee.png")
private const val READABILITY_HIGHLIGHT_PATH: String = "battlefield/tile_selection_4.png"

// One row per base terrain tile: tile 0 and tile 15 of every transition strip.
// Each cell is a 32x32 unit frame over that terrain repeated 2x2, so the part of a tall unit that rises above its
// tile is checked against the same terrain. Columns: each unit, then the selection highlight on the unit's tile.
// The right panel is the same sheet in grayscale, to check value contrast.
private suspend fun writeReadabilitySheet(): File {
    val transitionsPath = "terrain/transitions"
    val strips =
        resourcesVfs[transitionsPath]
            .listNames()
            .filter { it.endsWith(".png") }
            .sorted()
    val baseTiles =
        strips.flatMap { fileName ->
            val strip = loadBitmap32("$transitionsPath/$fileName")
            listOf(strip.tile(0), strip.tile(15))
        }
    val units = READABILITY_UNIT_PATHS.map { loadBitmap32(it) }
    val highlight = loadBitmap32(READABILITY_HIGHLIGHT_PATH)
    val columns = units.size + 1
    val rows = baseTiles.size
    val panelWidth = columns * CELL_SIZE + (columns - 1) * CELL_GAP
    val sheetWidth = PANEL_PADDING * 2 + panelWidth * 2 + PANEL_GAP
    val sheetHeight = PANEL_PADDING * 2 + rows * CELL_SIZE + (rows - 1) * CELL_GAP
    val sheet = Bitmap32(sheetWidth, sheetHeight)
    for (y in 0 until sheetHeight) {
        for (x in 0 until sheetWidth) {
            sheet[x, y] = SHEET_BACKGROUND
        }
    }
    baseTiles.forEachIndexed { row, baseTile ->
        val cellY = PANEL_PADDING + row * (CELL_SIZE + CELL_GAP)
        val sprites: List<Bitmap32> = units + listOf(highlight)
        sprites.forEachIndexed { column, sprite ->
            val ground = baseTile.repeated(CELL_ART_SIZE / TILE_PIXEL_SIZE)
            if (sprite.width == CELL_ART_SIZE) ground.overlay(sprite) else ground.overlay(sprite, left = TILE_PIXEL_SIZE / 2, top = TILE_PIXEL_SIZE)
            val cell = ground.scaled(SCALE)
            sheet.paste(cell, PANEL_PADDING + column * (CELL_SIZE + CELL_GAP), cellY)
            sheet.paste(cell.toGrayscale(), PANEL_PADDING + panelWidth + PANEL_GAP + column * (CELL_SIZE + CELL_GAP), cellY)
        }
    }
    val file = File(READABILITY_SHEET_PATH)
    file.parentFile?.mkdirs()
    file.writeBytes(PNG.encode(sheet, ImageEncodingProps(quality = 1.0)))
    return file
}

private fun Bitmap32.scaled(factor: Int): Bitmap32 {
    val result = Bitmap32(width * factor, height * factor)
    for (y in 0 until result.height) {
        for (x in 0 until result.width) {
            result[x, y] = this[x / factor, y / factor]
        }
    }
    return result
}

private fun Bitmap32.repeated(times: Int): Bitmap32 {
    val result = Bitmap32(width * times, height * times)
    for (y in 0 until result.height) {
        for (x in 0 until result.width) {
            result[x, y] = this[x % width, y % height]
        }
    }
    return result
}

// Draws opaque and semi-visible pixels of `sprite` on top of this bitmap at [left], [top], in place.
private fun Bitmap32.overlay(
    sprite: Bitmap32,
    left: Int = 0,
    top: Int = 0,
) {
    for (y in 0 until minOf(height - top, sprite.height)) {
        for (x in 0 until minOf(width - left, sprite.width)) {
            val pixel = sprite[x, y]
            if (pixel.a != 0) this[left + x, top + y] = pixel
        }
    }
}

private fun Bitmap32.paste(
    source: Bitmap32,
    left: Int,
    top: Int,
) {
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            this[left + x, top + y] = source[x, y]
        }
    }
}

private fun Bitmap32.toGrayscale(): Bitmap32 {
    val result = Bitmap32(width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this[x, y]
            val luma = (pixel.r * 299 + pixel.g * 587 + pixel.b * 114) / 1000
            result[x, y] = RGBA(luma, luma, luma, pixel.a)
        }
    }
    return result
}
