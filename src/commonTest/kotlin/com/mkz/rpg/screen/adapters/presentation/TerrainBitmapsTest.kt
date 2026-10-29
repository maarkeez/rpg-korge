package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class TerrainBitmapsTest : ViewsForTesting() {
    @Test
    fun `Bitmap should return the pure terrain tile of the strip when the tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap = terrainBitmaps.bitmap(terrainId = "sand", transition = null)
            // Then
            tileBitmap.width shouldBe BattlefieldView.TILE_PIXEL_SIZE
            tileBitmap.height shouldBe BattlefieldView.TILE_PIXEL_SIZE
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 0).shouldBeTrue()
        }

    @Test
    fun `Bitmap should return the pure target terrain tile of the strip when the target terrain tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap = terrainBitmaps.bitmap(terrainId = "void", transition = null)
            // Then
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15).shouldBeTrue()
        }

    @Test
    fun `Bitmap should return the transition tile of the strip when the tile has a terrain transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap =
                terrainBitmaps.bitmap(
                    terrainId = "sand",
                    transition = TerrainTransition(fromTerrainId = "sand", toTerrainId = "void", wangIndex = 9),
                )
            // Then
            tileBitmap.width shouldBe BattlefieldView.TILE_PIXEL_SIZE
            tileBitmap.height shouldBe BattlefieldView.TILE_PIXEL_SIZE
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 9).shouldBeTrue()
        }

    @Test
    fun `Bitmap should return the last tile of the strip when the terrain transition has all target terrain edges`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val transitionStrip = ResourcesBundle.root["terrain/transitions/sand_to_void.png"].readBitmap()
            // When
            val tileBitmap =
                terrainBitmaps.bitmap(
                    terrainId = "sand",
                    transition = TerrainTransition(fromTerrainId = "sand", toTerrainId = "void", wangIndex = 15),
                )
            // Then
            matchesWangTile(tileBitmap, transitionStrip, stripIndex = 15).shouldBeTrue()
        }

    @Test
    fun `Bitmap should throw when the terrain bitmap is not found and the tile has no terrain transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            // When
            val result = runCatching { terrainBitmaps.bitmap(terrainId = "forest", transition = null) }
            // Then
            result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
        }

    private fun matchesWangTile(
        tile: Bitmap,
        transitionStrip: Bitmap,
        stripIndex: Int,
    ): Boolean {
        val tileSize = BattlefieldView.TILE_PIXEL_SIZE
        val stripOffset = stripIndex * tileSize
        for (y in 0 until tileSize) {
            for (x in 0 until tileSize) {
                if (tile.getRgba(x, y) != transitionStrip.getRgba(stripOffset + x, y)) return false
            }
        }
        return true
    }
}
