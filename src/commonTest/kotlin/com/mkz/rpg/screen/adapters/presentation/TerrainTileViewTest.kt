package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class TerrainTileViewTest : ViewsForTesting() {
    private val tileSize = BattlefieldView.TILE_SIZE.toDouble()

    @Test
    fun `Display should draw the pure terrain tile when the terrain has no transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val terrainTileView = TerrainTileView(terrainBitmaps)
            // When
            terrainTileView.display(terrainId = "sand", transition = null)
            // Then
            terrainTileView.drawnBitmap() shouldBe terrainBitmaps.bitmap("sand", transition = null)
        }

    @Test
    fun `Display should draw the transition tile when the terrain has a transition`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val terrainTileView = TerrainTileView(terrainBitmaps)
            val transition = TerrainTransition(fromTerrainId = "sand", toTerrainId = "grass", wangIndex = 6)
            // When
            terrainTileView.display(terrainId = "sand", transition = transition)
            // Then
            terrainTileView.drawnBitmap() shouldBe terrainBitmaps.bitmap("sand", transition)
            terrainTileView.drawnBitmap() shouldNotBe terrainBitmaps.bitmap("sand", transition = null)
        }

    @Test
    fun `Display should replace the terrain drawn when another terrain is displayed`() =
        sharedViewsTest {
            // Given
            val terrainBitmaps = TerrainBitmaps(loadedResourcesLoader())
            val terrainTileView = TerrainTileView(terrainBitmaps)
            terrainTileView.display(terrainId = "sand", transition = null)
            // When
            terrainTileView.display(terrainId = "lava", transition = null)
            // Then
            terrainTileView.drawnBitmap() shouldBe terrainBitmaps.bitmap("lava", transition = null)
            terrainTileView.layerCount() shouldBe 1
        }

    @Test
    fun `Display should fill the tile at a whole scale when a terrain is displayed`() =
        sharedViewsTest {
            // Given
            val terrainTileView = TerrainTileView(TerrainBitmaps(loadedResourcesLoader()))
            // When
            terrainTileView.display(terrainId = "grass", transition = null)
            // Then
            terrainTileView.placement() shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize)
            (tileSize / BattlefieldView.TILE_PIXEL_SIZE) % 1.0 shouldBe 0.0
        }

    @Test
    fun `Display should draw the terrain without smoothing when a terrain is displayed`() =
        sharedViewsTest {
            // Given
            val terrainTileView = TerrainTileView(TerrainBitmaps(loadedResourcesLoader()))
            // When
            terrainTileView.display(terrainId = "water", transition = null)
            // Then
            terrainTileView.isSmoothed().shouldBeFalse()
        }
}
