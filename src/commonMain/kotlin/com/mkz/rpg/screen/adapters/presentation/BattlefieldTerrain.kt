package com.mkz.rpg.screen.adapters.presentation

/**
 * What [BattlefieldView] draws of a battlefield: its size, and the terrain of each of its tiles, with the transition
 * to the terrain around it. [BattlefieldPresenter] and [MapBuilderPresenter] map it from the domain.
 */
data class BattlefieldTerrain(
    val rows: Int,
    val columns: Int,
    val tiles: Map<TilePosition, Tile>,
) {
    /** The terrain of a tile, or its transition when the terrain around it differs. */
    data class Tile(
        val terrainId: String,
        val transition: TerrainTransition?,
    )
}

/** A tile between two terrains: the [wangIndex]th tile of the strip from the first terrain to the second one. */
data class TerrainTransition(
    val fromTerrainId: String,
    val toTerrainId: String,
    val wangIndex: Int,
)
