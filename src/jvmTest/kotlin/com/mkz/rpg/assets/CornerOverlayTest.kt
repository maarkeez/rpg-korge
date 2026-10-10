package com.mkz.rpg.assets

import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val CORNERS_PATH: String = "terrain/dual"
private const val EMPTY_MASK: Int = 0
private const val FULL_MASK: Int = 15

class CornerOverlayTest {
    private fun listCornerFiles(): List<String> =
        runBlocking {
            val root = resourcesVfs[CORNERS_PATH]
            if (root.exists()) root.listNames().filter { it.endsWith(".png") } else emptyList()
        }

    @Test
    fun `should only draw corner overlays for allowed terrain pairs when the corner folder is listed`() {
        // Given
        val loader = ResourcesTerrainLoader()
        runBlocking { loader.initResources() }
        val allowedPairs =
            loader
                .loadTerrains()
                .flatMap { terrain -> (terrain.allowedTransitionTo - terrain.id).map { "${terrain.id}_to_$it.png" } }
                .toSet()
        // When
        val cornerFiles = listCornerFiles()
        // Then
        assertThat(allowedPairs).containsAll(cornerFiles)
    }

    @Test
    fun `should leave the empty and the full corner transparent when every corner strip is checked`() {
        // Given
        val cornerFiles = listCornerFiles()
        // When
        val drawnWhereNothingChanges = mutableListOf<String>()
        runBlocking {
            for (fileName in cornerFiles) {
                val strip = loadBitmap32("$CORNERS_PATH/$fileName")
                for (mask in listOf(EMPTY_MASK, FULL_MASK)) {
                    val tile = strip.tile(mask)
                    val anyPixel = (0 until TILE_PIXEL_SIZE).any { y -> (0 until TILE_PIXEL_SIZE).any { x -> tile[x, y].a != 0 } }
                    if (anyPixel) drawnWhereNothingChanges += "$fileName mask $mask"
                }
            }
        }
        // Then
        assertThat(drawnWhereNothingChanges).describedAs("Corners with no edge must stay transparent so the base tiles show").isEmpty()
    }
}
