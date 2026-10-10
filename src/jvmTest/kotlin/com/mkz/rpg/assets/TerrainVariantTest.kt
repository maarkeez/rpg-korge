package com.mkz.rpg.assets

import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val VARIANTS_PATH: String = "terrain/variants"

class TerrainVariantTest {
    private fun listVariantFiles(): List<String> =
        runBlocking {
            val root = resourcesVfs[VARIANTS_PATH]
            if (root.exists()) root.listNames().filter { it.endsWith(".png") } else emptyList()
        }

    @Test
    fun `should name every variant strip after a terrain definition when the variants folder is listed`() {
        // Given
        val variantFiles = listVariantFiles()
        // When
        val unknownTerrains =
            runBlocking {
                variantFiles.filterNot { fileName -> resourcesVfs["terrain/${fileName.removeSuffix(".png")}.toml"].exists() }
            }
        // Then
        assertThat(unknownTerrains).describedAs("Variant strips without a terrain/<id>.toml:\n%s", unknownTerrains.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should be fully opaque 16x16 tiles when every variant strip is checked`() {
        // Given
        val variantFiles = listVariantFiles()
        // When
        val badStrips = mutableListOf<String>()
        runBlocking {
            for (fileName in variantFiles) {
                val strip = loadBitmap32("$VARIANTS_PATH/$fileName")
                val tiles = strip.width / TILE_PIXEL_SIZE
                val opaque = (0 until tiles).all { strip.tile(it).isFullyOpaque() }
                if (strip.height != TILE_PIXEL_SIZE || strip.width % TILE_PIXEL_SIZE != 0 || !opaque) badStrips += fileName
            }
        }
        // Then
        assertThat(badStrips).describedAs("Variant strips that are not opaque N x 16 x 16 tiles:\n%s", badStrips.joinToString("\n")).isEmpty()
    }
}
