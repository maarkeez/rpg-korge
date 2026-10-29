package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

/**
 * The terrain of a tile of the battlefield, e.g.: sand, or the tile between sand and grass, drawn at a whole scale to
 * fill the tile. It is the bottom layer of the tile: everything else is drawn over it.
 */
class TerrainTileView(
    private val terrainBitmaps: TerrainBitmaps,
) : Container() {
    internal val terrainImage: Image =
        image(Bitmap32(1, 1)) {
            smoothing = false
        }

    /** Draws the terrain, or its transition to another terrain, replacing what was drawn. */
    fun display(
        terrainId: String,
        transition: TerrainTransition?,
    ) {
        val bitmap = terrainBitmaps.bitmap(terrainId, transition)
        terrainImage.bitmap = bitmap.slice()
        terrainImage.scale = BattlefieldView.tileFillingScale(bitmap)
    }
}
