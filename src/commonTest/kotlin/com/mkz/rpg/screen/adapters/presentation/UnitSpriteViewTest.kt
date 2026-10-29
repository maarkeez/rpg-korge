package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import korlibs.image.bitmap.Bitmap32
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class UnitSpriteViewTest : ViewsForTesting() {
    private val tileSize = BattlefieldView.TILE_SIZE.toDouble()

    @Test
    fun `UnitSpriteView should draw the image of the unit when it is created`() =
        sharedViewsTest {
            // Given
            val knightBitmap = loadedResourcesLoader().bitmap("unit/knight.png")
            // When
            val unitSpriteView = UnitSpriteView(knightBitmap)
            // Then
            unitSpriteView.drawnBitmap() shouldBe knightBitmap
        }

    @Test
    fun `UnitSpriteView should fill the tile when its art is 16x16`() {
        // When
        val unitSpriteView = UnitSpriteView(Bitmap32(16, 16))
        // Then
        unitSpriteView.placement() shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize)
    }

    @Test
    fun `UnitSpriteView should fill the tile when its art is 32x32`() {
        // When
        val unitSpriteView = UnitSpriteView(Bitmap32(32, 32))
        // Then
        unitSpriteView.placement() shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize)
    }

    @Test
    fun `UnitSpriteView should scale 16x16 art four times and 32x32 art twice when it fills the tile`() {
        // Then
        UnitSpriteView(Bitmap32(16, 16)).scale() shouldBe 4.0
        UnitSpriteView(Bitmap32(32, 32)).scale() shouldBe 2.0
    }

    @Test
    fun `UnitSpriteView should draw the image without smoothing when it is created`() {
        // When
        val unitSpriteView = UnitSpriteView(Bitmap32(16, 16))
        // Then
        unitSpriteView.isSmoothed().shouldBeFalse()
    }
}
