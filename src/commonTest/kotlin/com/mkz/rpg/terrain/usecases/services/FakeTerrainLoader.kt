package com.mkz.rpg.terrain.usecases.services

import com.mkz.rpg.terrain.domain.Terrain

class FakeTerrainLoader(
    private val terrains: List<Terrain.Dto> = emptyList(),
) : TerrainLoader {
    override fun loadTerrains(): List<Terrain.Dto> = terrains
}
