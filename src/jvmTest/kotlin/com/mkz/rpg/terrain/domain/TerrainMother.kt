package com.mkz.rpg.terrain.domain

object TerrainMother {
    fun occupiableTerrain(id: String = terrainId()) =
        Terrain.create(
            Terrain.Dto(
                id = id,
                canBeOccupied = true,
            ),
        )

    fun nonOccupiableTerrain(id: String = terrainId()) =
        Terrain.create(
            Terrain.Dto(
                id = id,
                canBeOccupied = false,
            ),
        )

    fun terrainId() = listOf("sand", "void").random()
}
