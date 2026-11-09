package com.mkz.rpg.terrain.domain

object TerrainMother {
    fun terrainThatCanBeOccupied(
        id: String = terrainId(),
        transitionsTo: Set<String> = emptySet(),
        effectId: String? = null,
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = true,
                transitionsTo = transitionsTo,
                effectId = effectId,
            ),
        )

    fun terrainThatCanNotBeOccupied(
        id: String = terrainId(),
        transitionsTo: Set<String> = emptySet(),
        effectId: String? = null,
    ) = Terrain
        .create(
            Terrain.Dto(
                id = id,
                canBeOccupied = false,
                transitionsTo = transitionsTo,
                effectId = effectId,
            ),
        )

    fun terrainId() = listOf("sand", "void").random()
}
