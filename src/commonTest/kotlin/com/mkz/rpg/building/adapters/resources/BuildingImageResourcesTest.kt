package com.mkz.rpg.building.adapters.resources

import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import korlibs.image.format.readBitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

// The game logic does not depend on the images, so the size of a building is declared in its data: this checks its image matches it.
class BuildingImageResourcesTest {
    @Test
    fun `should find the image of every building in the resources, of 16 pixels per tile`() =
        runTest {
            // Given
            val buildings = ResourcesBuildingLoader().also { loader -> loader.initResources() }.loadBuildings()
            // When
            val mismatches =
                buildings.mapNotNull { building ->
                    val imageFile = ResourcesBundle.root["building/${building.id}.png"]
                    if (!imageFile.exists()) return@mapNotNull "${building.id}: no image"
                    val image = imageFile.readBitmap()
                    val expectedWidth = building.columns * TILE_PIXEL_SIZE
                    val expectedHeight = building.rows * TILE_PIXEL_SIZE
                    if (image.width == expectedWidth && image.height == expectedHeight) return@mapNotNull null
                    "${building.id}: ${image.width}x${image.height} instead of ${expectedWidth}x$expectedHeight"
                }
            // Then
            buildings.shouldNotBeEmpty()
            mismatches.shouldBeEmpty()
        }

    private companion object {
        const val TILE_PIXEL_SIZE = 16
    }
}
