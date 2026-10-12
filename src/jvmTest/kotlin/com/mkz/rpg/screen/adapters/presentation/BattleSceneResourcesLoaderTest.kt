package com.mkz.rpg.screen.adapters.presentation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.PNG
import korlibs.io.file.std.MemoryVfsMix
import korlibs.korge.tests.ViewsForTesting
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
                resourcesLoader.bitmap("unit/knight.png").width shouldBe 1
                resourcesLoader.bitmap("unit/portraits/knight_portrait.png").width shouldBe 2
                resourcesLoader.bitmap("card.jpg").width shouldBe 3
            }

        @Test
        fun `should ignore files that are not images when resources are loaded`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot())
                // When
                resourcesLoader.load()
                // Then
                resourcesLoader.bitmapOrNull("terrain/sand.toml").shouldBeNull()
            }

        @Test
        fun `should ignore hidden files when resources are loaded`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot())
                // When
                resourcesLoader.load()
                // Then
                resourcesLoader.bitmapOrNull("unit/.hidden.png").shouldBeNull()
            }

        @Test
        fun `should load the game images when resources are loaded from the default root`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader()
                // When
                resourcesLoader.load()
                // Then
                resourcesLoader.bitmapOrNull("unit/knight.png").shouldNotBeNull()
                resourcesLoader.bitmaps("terrain/transitions").shouldNotBeEmpty()
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
                secondBitmap.shouldBeSameInstanceAs(firstBitmap)
            }

        @Test
        fun `should fail with the requested path when the image does not exist`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmapRequest = { resourcesLoader.bitmap("unit/dragon.png") }
                // Then
                shouldThrow<IllegalStateException> { bitmapRequest() }.message.shouldContain("unit/dragon.png")
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
                bitmap.shouldBeNull()
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
                bitmaps.keys.shouldContainExactly("knight.png")
            }

        @Test
        fun `should return no images when the directory does not exist`() =
            viewsTest {
                // Given
                val resourcesLoader = BattleSceneResourcesLoader(resourcesRoot()).also { it.load() }
                // When
                val bitmaps = resourcesLoader.bitmaps("dragons")
                // Then
                bitmaps.shouldBeEmpty()
            }
    }
}
