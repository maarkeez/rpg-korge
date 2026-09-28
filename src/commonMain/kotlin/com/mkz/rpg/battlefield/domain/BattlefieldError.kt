package com.mkz.rpg.battlefield.domain

sealed class BattlefieldError(
    message: String,
) : Throwable(message = message) {
    class TileIsNotVacant : BattlefieldError("Tile is not vacant")

    class TileNotFound : BattlefieldError("Tile not found")

    class TileTerrainTransitionNotAllowed(
        val terrainId: String,
        val transitionToTerrainId: String,
    ) : BattlefieldError("Tile terrain transition from '$terrainId' to '$transitionToTerrainId' is not allowed")
}
