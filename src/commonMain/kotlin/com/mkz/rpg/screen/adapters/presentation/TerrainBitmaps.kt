package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

/**
 * The art of the terrain tiles, shared by every [TerrainTileView]: each transition strip of the resources is split into
 * its tiles, the first and the last ones being the pure terrain tiles. They are split the first time they are asked for.
 */
class TerrainBitmaps(
    private val resourcesLoader: BattleSceneResourcesLoader,
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

    /** The tile of the terrain, or of its [transition] to another terrain, when it has one. */
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
