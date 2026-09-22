package com.mkz.rpg.terrain.domain

sealed class TerrainError(
    message: String,
) : Throwable(message = message) {
    class TerrainAlreadyExists : TerrainError(message = "Terrain already exists")
}
