package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import korlibs.image.format.ImageEncodingProps
import korlibs.image.format.PNG
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("contact-sheet")
class AssetContactSheetTest {
    @Test
    fun `should draw every asset into a single contact sheet image`() {
        // When
        val file = runBlocking { writeContactSheet() }
        // Then
        assertThat(file).exists()
        assertThat(file.length()).isGreaterThan(0)
    }
}

private const val SHEET_WIDTH: Int = 512
private const val SHEET_PADDING: Int = 8
private val SHEET_BACKGROUND: RGBA = RGBA(0xFF211640.toInt())

private suspend fun writeContactSheet(): File {
    val assets = CONTACT_SHEET_ASSET_PATHS.map { path -> path to resourcesVfs[path].readBitmap().toBMP32() }
    val sheet = drawContactSheet(assets)
    val file = File(CONTACT_SHEET_PATH)
    file.parentFile?.mkdirs()
    file.writeBytes(PNG.encode(sheet, ImageEncodingProps(quality = 1.0)))
    return file
}

private data class Placement(
    val x: Int,
    val y: Int,
    val bitmap: Bitmap32,
)

private fun drawContactSheet(assets: List<Pair<String, Bitmap32>>): Bitmap32 {
    val placements = shelfLayout(assets.map { it.second })
    val sheetHeight = placements.maxOf { it.y + it.bitmap.height } + SHEET_PADDING
    val sheet = Bitmap32(SHEET_WIDTH, sheetHeight)
    for (y in 0 until sheetHeight) {
        for (x in 0 until SHEET_WIDTH) {
            sheet.set(x, y, SHEET_BACKGROUND)
        }
    }
    assets.zip(placements).forEach { (asset, placement) ->
        for (y in 0 until asset.second.height) {
            for (x in 0 until asset.second.width) {
                val pixel = asset.second[x, y]
                if (pixel.a == 0) continue
                sheet.set(placement.x + x, placement.y + y, pixel)
            }
        }
    }
    return sheet
}

private fun shelfLayout(bitmaps: List<Bitmap32>): List<Placement> {
    val placements = ArrayList<Placement>()
    var x = SHEET_PADDING
    var y = SHEET_PADDING
    var shelfHeight = 0
    bitmaps.forEach { bitmap ->
        if (x + bitmap.width > SHEET_WIDTH - SHEET_PADDING && placements.isNotEmpty()) {
            x = SHEET_PADDING
            y += shelfHeight + SHEET_PADDING
            shelfHeight = 0
        }
        placements += Placement(x, y, bitmap)
        x += bitmap.width + SHEET_PADDING
        shelfHeight = maxOf(shelfHeight, bitmap.height)
    }
    return placements
}
