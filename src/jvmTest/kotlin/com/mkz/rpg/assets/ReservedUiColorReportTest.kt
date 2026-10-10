package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

// Hues that UiPalette uses for states (see docs/design/asset-redesign-plan.md §6.3).
// Art that is dominated by one of them can be confused with a UI state.
private val RESERVED_UI_COLORS: Map<Int, String> =
    mapOf(
        0xE03C28 to "enemy / danger / hpLoss",
        0x0084FF to "ally / mana",
        0x5BA8FF to "manaCost (next to ally / move)",
        0xFFE737 to "selection",
        0x98DCFF to "move",
        0xFFBB31 to "cast",
        0xF68F37 to "cast",
        0xD59CFC to "conditional",
        0xA328B3 to "missing-asset placeholder",
    )

private const val DOMINANT_SHARE: Double = 0.40
private const val REPORT_PATH: String = "build/reports/asset-reserved-color-report.txt"

class ReservedUiColorReportTest {
    // Informational: this test never fails on colour. It writes the report for review.
    @Test
    fun `should write the reserved UI colour report when game art is checked`() {
        // Given
        val file = File(REPORT_PATH)
        // When
        val lines = runBlocking { buildReservedColorReport() }
        file.parentFile?.mkdirs()
        file.writeText(lines.joinToString("\n", postfix = "\n"))
        // Then
        assertThat(file).exists()
        assertThat(file.readLines()).isNotEmpty()
    }

    private suspend fun buildReservedColorReport(): List<String> {
        val dominantLines = mutableListOf<String>()
        val findingLines = mutableListOf<String>()
        for ((label, bitmap) in reportSubjects()) {
            val opaqueCounts = HashMap<Int, Int>()
            var opaquePixels = 0
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    val pixel = bitmap[x, y]
                    if (pixel.a == 0) continue
                    opaquePixels++
                    // RGBA.rgb is BGR-ordered in this korlibs version, so build the 0xRRGGBB key explicitly.
                    val key = (pixel.r shl 16) or (pixel.g shl 8) or pixel.b
                    opaqueCounts[key] = (opaqueCounts[key] ?: 0) + 1
                }
            }
            opaqueCounts.maxByOrNull { it.value }?.let { (color, count) ->
                dominantLines += "  $label: ${hex(color)} ${percent(count, opaquePixels)}"
            }
            for ((color, name) in RESERVED_UI_COLORS) {
                val share = (opaqueCounts[color] ?: 0).toDouble() / opaquePixels
                if (share > DOMINANT_SHARE) {
                    findingLines += "  $label: ${hex(color)} ($name) ${percent(opaqueCounts[color] ?: 0, opaquePixels)}"
                }
            }
        }
        return buildList {
            add("Dominant colour per asset (baseline):")
            addAll(dominantLines)
            add("")
            add("Reserved UI colours above ${(DOMINANT_SHARE * 100).toInt()}% of opaque pixels:")
            if (findingLines.isEmpty()) add("  none") else addAll(findingLines)
        }
    }

    private fun hex(color: Int): String = "#" + color.toString(16).padStart(6, '0').uppercase()

    private fun percent(
        count: Int,
        total: Int,
    ): String = "%.1f%%".format(count * 100.0 / total)

    // Whole assets, plus the base terrain tiles of each transition strip (tile 0 and tile 15),
    // because a strip mixes two terrains and its whole-image share hides a dominant base tile.
    private suspend fun reportSubjects(): List<Pair<String, Bitmap32>> {
        val wholeAssets = GAME_ART_ASSET_PATHS.map { path -> path to loadBitmap32(path) }
        val baseTiles =
            GAME_ART_ASSET_PATHS
                .filter { it.startsWith("terrain/transitions/") }
                .flatMap { path ->
                    val strip = loadBitmap32(path)
                    listOf("$path [tile 0]" to strip.tile(0), "$path [tile 15]" to strip.tile(15))
                }
        return wholeAssets + baseTiles
    }
}
