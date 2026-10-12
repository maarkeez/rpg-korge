package com.mkz.rpg.terrain.usecases.services

import com.mkz.rpg.terrain.domain.Terrain

// Loads the given terrains, instead of reading them from the resources.
class FakeTerrainLoader(
    private val terrains: List<Terrain.Dto> = emptyList(),
) : TerrainLoader {
    override fun loadTerrains(): List<Terrain.Dto> = terrains
}
