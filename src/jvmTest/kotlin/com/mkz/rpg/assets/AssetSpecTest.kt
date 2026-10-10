package com.mkz.rpg.assets

import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssetSpecTest {
    @Test
    fun `should decode every game art asset when the asset file exists`() {
        // Given
        val missingOrBroken = mutableListOf<String>()
        // When
        runBlocking {
            for (path in GAME_ART_ASSET_PATHS) {
                try {
                    if (!resourcesVfs[path].exists()) {
                        missingOrBroken += "$path: file not found"
                    } else {
                        loadBitmap32(path)
                    }
                } catch (exception: Exception) {
                    missingOrBroken += "$path: not a decodable PNG (${exception.message})"
                }
            }
        }
        // Then
        assertThat(missingOrBroken).describedAs("Game art assets that cannot be decoded:\n%s", missingOrBroken.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should match the family size when every game art asset is listed`() {
        // Given
        val wrongSizes = mutableListOf<String>()
        // When
        runBlocking {
            for (path in GAME_ART_ASSET_PATHS) {
                val expected = expectedArtSize(path)
                if (expected == null) {
                    wrongSizes += "$path: no family in §6.1 for this folder"
                    continue
                }
                val bitmap = loadBitmap32(path)
                if (!expected.matches(bitmap.width, bitmap.height)) {
                    wrongSizes += "$path: expected $expected, was ${bitmap.width}x${bitmap.height}"
                }
            }
        }
        // Then
        assertThat(wrongSizes).describedAs("Game art assets with the wrong size:\n%s", wrongSizes.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should use only fully opaque or fully transparent pixels when every game art asset is checked`() {
        // Given
        val partialAlphaAssets = mutableListOf<String>()
        // When
        runBlocking {
            for (path in GAME_ART_ASSET_PATHS) {
                if (loadBitmap32(path).hasPartialAlpha()) {
                    partialAlphaAssets += path
                }
            }
        }
        // Then
        assertThat(partialAlphaAssets).describedAs("Game art assets with partial alpha pixels:\n%s", partialAlphaAssets.joinToString("\n")).isEmpty()
    }
}
