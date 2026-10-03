package com.mkz.rpg.terrain.domain

object TerrainMother {
    fun occupiableTerrain(
        id: String = terrainId(),
        allowedTransitionTo: Set<String> = emptySet(),
        effectId: String? = null,
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = true,
                allowedTransitionTo = allowedTransitionTo,
                effectId = effectId,
            ),
        )

    fun nonOccupiableTerrain(
        id: String = terrainId(),
        allowedTransitionTo: Set<String> = emptySet(),
        effectId: String? = null,
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = false,
                allowedTransitionTo = allowedTransitionTo,
                effectId = effectId,
            ),
        )

    fun terrainId() = listOf("sand", "void").random()
}
