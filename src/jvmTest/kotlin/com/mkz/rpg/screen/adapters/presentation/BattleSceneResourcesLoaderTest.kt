package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.PNG
import korlibs.io.file.std.MemoryVfsMix
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleSceneResourcesLoaderTest : ViewsForTesting() {
    // Each image has a distinct width, so the tests can tell them apart.
    private fun resourcesRoot() =
        MemoryVfsMix(
            "unit/knight.png" to png(width = 1),
            "unit/portraits/knight_portrait.png" to png(width = 2),
            "card.jpg" to png(width = 3),
            "terrain/sand.toml" to "canBeOccupied = true",
            "unit/.hidden.png" to png(width = 4),
        )

    private fun png(width: Int): ByteArray = PNG.encode(Bitmap32(width, 1, premultiplied = true))

    @Nested
    inner class Load {
        @Test
        fun `should load images of nested directories when resources are loaded`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot())
                // When
                resourcesLoader.load()
                // Then
                assertThat(resourcesLoader.bitmap("unit/knight.png").width).isEqualTo(1)
                assertThat(resourcesLoader.bitmap("unit/portraits/knight_portrait.png").width).isEqualTo(2)
                assertThat(resourcesLoader.bitmap("card.jpg").width).isEqualTo(3)
            }

        @Test
        fun `should ignore files that are not images when resources are loaded`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot())
                // When
                resourcesLoader.load()
                // Then
                assertThat(resourcesLoader.bitmapOrNull("terrain/sand.toml")).isNull()
            }

        @Test
        fun `should ignore hidden files when resources are loaded`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot())
                // When
                resourcesLoader.load()
                // Then
                assertThat(resourcesLoader.bitmapOrNull("unit/.hidden.png")).isNull()
            }

        @Test
        fun `should load the game images when resources are loaded from the default root`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader()
                // When
                resourcesLoader.load()
                // Then
                assertThat(resourcesLoader.bitmapOrNull("unit/knight.png")).isNotNull
                assertThat(resourcesLoader.bitmaps("terrain/transitions")).isNotEmpty
            }
    }

    @Nested
    inner class Bitmap {
        @Test
        fun `should return the same image instance when the image is requested twice`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val firstBitmap = resourcesLoader.bitmap("unit/knight.png")
                val secondBitmap = resourcesLoader.bitmap("unit/knight.png")
                // Then
                assertThat(secondBitmap).isSameAs(firstBitmap)
            }

        @Test
        fun `should fail with the requested path when the image does not exist`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmapRequest = { resourcesLoader.bitmap("unit/dragon.png") }
                // Then
                assertThatThrownBy { bitmapRequest() }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessageContaining("unit/dragon.png")
            }
    }

    @Nested
    inner class BitmapOrNull {
        @Test
        fun `should return null when the image does not exist`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmap = resourcesLoader.bitmapOrNull("unit/dragon.png")
                // Then
                assertThat(bitmap).isNull()
            }
    }

    @Nested
    inner class Bitmaps {
        @Test
        fun `should return the images directly inside the directory keyed by file name when the directory is requested`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmaps = resourcesLoader.bitmaps("unit")
                // Then
                assertThat(bitmaps.keys).containsExactly("knight.png")
            }

        @Test
        fun `should return no images when the directory does not exist`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmaps = resourcesLoader.bitmaps("dragons")
                // Then
                assertThat(bitmaps).isEmpty()
            }
    }
}
