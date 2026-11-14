package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

class TerrainTileView(
    private val terrainBitmaps: TerrainBitmaps,
) : Container() {
    internal val terrainImage: Image =
        image(Bitmap32(1, 1)) {
            smoothing = false
        }

    fun display(
        terrainId: String,
        transition: TerrainTransition?,
    ) {
        val bitmap = terrainBitmaps.bitmap(terrainId, transition)
        terrainImage.bitmap = bitmap.slice()
        terrainImage.scale = BattlefieldView.tileFillingScale(bitmap)
    }
}
