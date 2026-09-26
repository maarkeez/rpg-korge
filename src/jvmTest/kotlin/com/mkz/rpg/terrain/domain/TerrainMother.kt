package com.mkz.rpg.terrain.domain

object TerrainMother {
    fun occupiableTerrain(
        id: String = terrainId(),
        allowedTransitionTo: Set<String> = emptySet(),
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = true,
                allowedTransitionTo = allowedTransitionTo,
            ),
        )

    fun nonOccupiableTerrain(
        id: String = terrainId(),
        allowedTransitionTo: Set<String> = emptySet(),
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = false,
                allowedTransitionTo = allowedTransitionTo,
            ),
        )

    fun terrainId() = listOf("sand", "void").random()
}
