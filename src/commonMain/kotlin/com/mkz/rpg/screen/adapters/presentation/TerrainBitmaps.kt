package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

class TerrainBitmaps(
    private val resourcesLoader: GameSceneResourcesLoader,
) {
    private companion object {
        const val TRANSITIONS_DIRECTORY = "terrain/transitions"
        const val TRANSITION_NAME_SEPARATOR = "_to_"
    }

    private val transitionBitmaps: Map<String, List<Bitmap>> by lazy {
        resourcesLoader
            .bitmaps(TRANSITIONS_DIRECTORY)
            .filterKeys { fileName -> fileName.endsWith(".png") }
            .map { (fileName, transitionBitmap) ->
                fileName.removeSuffix(".png") to
                    List(transitionBitmap.width / BattlefieldView.TILE_PIXEL_SIZE) { index ->
                        transitionBitmap.crop(index * BattlefieldView.TILE_PIXEL_SIZE, 0, BattlefieldView.TILE_PIXEL_SIZE, BattlefieldView.TILE_PIXEL_SIZE)
                    }
            }.toMap()
    }
    private val terrainBitmaps: Map<String, Bitmap> by lazy {
        buildMap {
            transitionBitmaps.forEach { (transitionName, transitionTiles) ->
                val (fromTerrainId, toTerrainId) = transitionName.split(TRANSITION_NAME_SEPARATOR)
                put(fromTerrainId, transitionTiles.first())
                put(toTerrainId, transitionTiles.last())
            }
        }
    }

    fun bitmap(
        terrainId: String,
        transition: TerrainTransition?,
    ): Bitmap =
        if (transition != null) {
            val transitionName = "${transition.fromTerrainId}$TRANSITION_NAME_SEPARATOR${transition.toTerrainId}"
            transitionBitmaps.getValue(transitionName)[transition.wangIndex]
        } else {
            terrainBitmaps[terrainId] ?: throw IllegalStateException("Terrain $terrainId bitmap not found")
        }
}
