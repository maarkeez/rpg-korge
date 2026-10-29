package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class TileHighlightViewTest : ViewsForTesting() {
    private val tileSize = BattlefieldView.TILE_SIZE.toDouble()

    @Test
    fun `TileHighlightView should draw the frame of the tiles to move to when it is the move range`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            // When
            val tileHighlightView = TileHighlightView(resourcesLoader, TileHighlightView.Kind.MOVE_RANGE)
            // Then
            tileHighlightView.drawnBitmap() shouldBe resourcesLoader.bitmap("battlefield/tile_selection_3.png")
        }

    @Test
    fun `TileHighlightView should draw the frame of the tiles to cast on when it is the cast range`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            // When
            val tileHighlightView = TileHighlightView(resourcesLoader, TileHighlightView.Kind.CAST_RANGE)
            // Then
            tileHighlightView.drawnBitmap() shouldBe resourcesLoader.bitmap("battlefield/tile_selection_2.png")
        }

    @Test
    fun `TileHighlightView should draw the frame of the selected tile when it is the selected tile`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            // When
            val tileHighlightView = TileHighlightView(resourcesLoader, TileHighlightView.Kind.SELECTED)
            // Then
            tileHighlightView.drawnBitmap() shouldBe resourcesLoader.bitmap("battlefield/tile_selection_4.png")
        }

    @Test
    fun `TileHighlightView should draw a frame of its own for each kind when it is created`() =
        sharedViewsTest {
            // When
            val bitmaps = TileHighlightView.Kind.entries.map { kind -> TileHighlightView(loadedResourcesLoader(), kind).drawnBitmap() }
            // Then
            bitmaps.distinct() shouldHaveSize TileHighlightView.Kind.entries.size
        }

    @Test
    fun `TileHighlightView should fill the tile when it is created`() =
        sharedViewsTest {
            // When
            val tileHighlightView = TileHighlightView(loadedResourcesLoader(), TileHighlightView.Kind.SELECTED)
            // Then
            tileHighlightView.placement() shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize)
        }
}
