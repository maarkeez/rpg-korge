package com.mkz.tessavar.screen.adapters.presentation

data class BattlefieldTerrain(
    val rows: Int,
    val columns: Int,
    val tiles: Map<TilePosition, Tile>,
) {
    data class Tile(
        val terrainId: String,
        val transition: TerrainTransition?,
    )
}

data class TerrainTransition(
    val fromTerrainId: String,
    val toTerrainId: String,
    val wangIndex: Int,
)
