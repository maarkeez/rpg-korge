package com.mkz.rpg.assets

import com.mkz.rpg.screen.SpriteRegistry
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// Animation strips are optional. When present they must follow the strip convention; when absent the registry uses the single frame.
private val UNIT_ANIMATIONS = SpriteRegistry.UNIT_ANIMATIONS

class SpriteRegistryCoverageTest {
    @Test
    fun `should find a file for every registered id when the registry id lists are checked`() {
        // Given
        val registeredPaths =
            SpriteRegistry.UNIT_IDS.flatMap { unitId -> listOf(SpriteRegistry.unitPath(unitId), SpriteRegistry.portraitPath(unitId)) } +
                SpriteRegistry.ABILITY_IDS.map(SpriteRegistry::abilityPath) +
                SpriteRegistry.EFFECT_IDS.map(SpriteRegistry::effectPath) +
                SpriteRegistry.Highlight.entries.map { it.path }
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
    fun `should be a row of 32x32 frames starting with the unit sprite when a unit animation strip exists`() {
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
                    if (strip.height != UNIT_FRAME_SIZE || strip.width % UNIT_FRAME_SIZE != 0) {
                        violations += "$stripPath: expected N x 32 x 32, was ${strip.width}x${strip.height}"
                    }
                    // Only idle must start on the standing sprite (art direction §1); walk frames are mid-step poses.
                    val startsOnSprite = animation == SpriteRegistry.IDLE
                    if (startsOnSprite && strip.width >= UNIT_FRAME_SIZE && !strip.frame(0, UNIT_FRAME_SIZE).isPixelIdentical(loadBitmap32(SpriteRegistry.unitPath(unitId)))) {
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
