package com.mkz.rpg.assets

import com.mkz.rpg.screen.SpriteRegistry
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// Animation strips are optional (M5). When present they must follow the strip convention; when absent the registry uses the single frame.
private val UNIT_ANIMATIONS = listOf("idle", "walk")

class SpriteRegistryCoverageTest {
    @Test
    fun `should find a file for every registered id when the registry id lists are checked`() {
        // Given
        val registeredPaths =
            SpriteRegistry.UNIT_IDS.flatMap { unitId -> listOf(SpriteRegistry.unitPath(unitId), SpriteRegistry.portraitPath(unitId)) } +
                SpriteRegistry.ABILITY_IDS.map(SpriteRegistry::abilityPath) +
                SpriteRegistry.EFFECT_IDS.map(SpriteRegistry::effectPath) +
                SpriteRegistry.Highlight.entries.map { it.path } +
                "ability/ability_selection.png"
        // When
        val missingPaths =
            runBlocking {
                registeredPaths.filterNot { path -> resourcesVfs[path].exists() }
            }
        // Then
        assertThat(missingPaths)
            .describedAs("Registered ids without a file (they would render the magenta placeholder):\n%s", missingPaths.joinToString("\n"))
            .isEmpty()
    }

    @Test
    fun `should keep frame 0 equal to the unit sprite and width a multiple of 16 when a unit animation strip exists`() {
        // Given
        val unitIds = SpriteRegistry.UNIT_IDS
        // When
        val violations = mutableListOf<String>()
        runBlocking {
            for (unitId in unitIds) {
                for (animation in UNIT_ANIMATIONS) {
                    val stripPath = "unit/${unitId}_$animation.png"
                    if (!resourcesVfs[stripPath].exists()) continue
                    val strip = loadBitmap32(stripPath)
                    if (strip.height != TILE_PIXEL_SIZE || strip.width % TILE_PIXEL_SIZE != 0) {
                        violations += "$stripPath: expected N x 16 x 16, was ${strip.width}x${strip.height}"
                    }
                    if (strip.width >= TILE_PIXEL_SIZE && !strip.tile(0).isPixelIdentical(loadBitmap32(SpriteRegistry.unitPath(unitId)))) {
                        violations += "$stripPath: frame 0 differs from ${SpriteRegistry.unitPath(unitId)}"
                    }
                }
            }
        }
        // Then
        assertThat(violations).describedAs("Unit animation strips that break the strip convention:\n%s", violations.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should have exactly three 16x16 frames when the defeat fx strip exists`() {
        // Given
        val stripPath = SpriteRegistry.fxPath(SpriteRegistry.FX_DEFEAT)
        // When
        val strip = runBlocking { if (resourcesVfs[stripPath].exists()) loadBitmap32(stripPath) else null }
        // Then
        if (strip != null) {
            assertThat(strip.width).describedAs("$stripPath must match POOF_FRAMES = 3").isEqualTo(3 * TILE_PIXEL_SIZE)
            assertThat(strip.height).isEqualTo(TILE_PIXEL_SIZE)
        }
    }
}
