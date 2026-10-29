package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class TileDimViewTest : ViewsForTesting() {
    private val tileSize = BattlefieldView.TILE_SIZE.toDouble()

    @Test
    fun `TileDimView should cover the tile with a black mask when it is created`() {
        // When
        val tileDimView = TileDimView()
        // Then
        tileDimView.maskColor() shouldBe Colors.BLACK
        tileDimView.maskSize() shouldBe (tileSize to tileSize)
    }

    @Test
    fun `TileDimView should darken what is under it a bit when it is created`() {
        // When
        val tileDimView = TileDimView()
        // Then
        tileDimView.maskAlpha() shouldBe (BattlefieldView.DIM_ALPHA plusOrMinus 0.001)
    }
}
