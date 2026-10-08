package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PaletteCheckerTest {
    @Test
    fun `should load the Famicube palette with 64 distinct colors`() {
        // Given
        val palette = runBlocking { loadFamicubePalette() }
        // Then
        assertThat(palette).hasSize(64)
    }

    @Test
    fun `should report off-palette pixels with color and count when a bitmap deviates from the palette`() {
        // Given
        val palette = setOf(0x151515, 0x000000)
        val bitmap = Bitmap32(3, 1)
        bitmap.set(0, 0, RGBA(0xFF151515.toInt()))
        bitmap.set(1, 0, RGBA(0xFFFF00FF.toInt()))
        bitmap.set(2, 0, RGBA(0xFFFF00FF.toInt()))
        // When
        val violations = bitmap.checkPalette(palette)
        // Then
        assertThat(violations).containsExactly(PaletteViolation(color = 0xFF00FF, count = 2))
        assertThat(describeViolations("unit/knight.png", violations)).isEqualTo("unit/knight.png: #FF00FF x 2")
    }

    @Test
    fun `should ignore transparent pixels when checking a bitmap against the palette`() {
        // Given
        val palette = setOf(0x151515)
        val bitmap = Bitmap32(2, 1)
        bitmap.set(0, 0, RGBA(0x00FF0000))
        bitmap.set(1, 0, RGBA(0xFF151515.toInt()))
        // When
        val violations = bitmap.checkPalette(palette)
        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should pass when every game art asset uses only Famicube palette colors`() {
        // Given
        val palette = runBlocking { loadFamicubePalette() }
        // When
        val failures = HashMap<String, List<PaletteViolation>>()
        runBlocking {
            for (path in GAME_ART_ASSET_PATHS) {
                val violations = checkAsset(path, palette)
                if (violations.isNotEmpty()) {
                    failures[path] = violations
                }
            }
        }
        // Then
        val message =
            "Game art assets with off-palette pixels:\n" +
                failures.entries
                    .sortedBy { it.key }
                    .joinToString("\n") { entry -> "  ${describeViolations(entry.key, entry.value)}" }
        assertTrue(failures.isEmpty(), message)
    }
}
