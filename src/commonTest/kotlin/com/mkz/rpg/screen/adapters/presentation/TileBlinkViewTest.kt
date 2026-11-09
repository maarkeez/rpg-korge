package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.shouldBe
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class TileBlinkViewTest : ViewsForTesting() {
    private val tileSize = BattlefieldView.TILE_SIZE.toDouble()

    private fun unitBitmap(): Bitmap32 =
        Bitmap32(2, 2).also { bitmap ->
            bitmap.setRgba(0, 0, RGBA(200, 30, 30, 255))
            bitmap.setRgba(1, 1, RGBA(30, 200, 30, 128))
        }

    @Test
    fun `TileBlinkView should draw the opaque pixels of the unit in white when it is created`() {
        // When
        val tileBlinkView = TileBlinkView(unitBitmap())
        // Then
        val silhouette = tileBlinkView.drawnBitmap()
        silhouette.getRgba(0, 0) shouldBe Colors.WHITE
        silhouette.getRgba(1, 1) shouldBe Colors.WHITE
    }

    @Test
    fun `TileBlinkView should keep the transparent pixels of the unit transparent when it is created`() {
        // When
        val tileBlinkView = TileBlinkView(unitBitmap())
        // Then
        val silhouette = tileBlinkView.drawnBitmap()
        silhouette.getRgba(1, 0).a shouldBe 0
        silhouette.getRgba(0, 1).a shouldBe 0
    }

    @Test
    fun `TileBlinkView should not change the image of the unit when it is created`() {
        // Given
        val unitBitmap = unitBitmap()
        // When
        TileBlinkView(unitBitmap)
        // Then
        unitBitmap.getRgba(0, 0) shouldBe RGBA(200, 30, 30, 255)
    }

    @Test
    fun `TileBlinkView should fill the tile as the unit does when it is created`() {
        // When
        val tileBlinkView = TileBlinkView(unitBitmap())
        // Then
        tileBlinkView.placement() shouldBe TilePlacement(x = 0.0, y = 0.0, width = tileSize, height = tileSize)
    }
}
