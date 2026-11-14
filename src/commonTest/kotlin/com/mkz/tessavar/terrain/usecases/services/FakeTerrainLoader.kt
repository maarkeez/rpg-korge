package com.mkz.tessavar.terrain.usecases.services

import com.mkz.tessavar.terrain.domain.Terrain

class FakeTerrainLoader(
    private val terrains: List<Terrain.Dto> = emptyList(),
) : TerrainLoader {
    override fun loadTerrains(): List<Terrain.Dto> = terrains
}
