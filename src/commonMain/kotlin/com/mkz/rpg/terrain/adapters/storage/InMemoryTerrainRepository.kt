package com.mkz.rpg.terrain.adapters.storage

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainRepository

class InMemoryTerrainRepository : TerrainRepository {
    private val terrains = mutableMapOf<String, Terrain>()

    override fun create(terrain: Terrain) {
        terrains[terrain.toDto().id] = terrain
    }

    override fun searchById(id: String): Terrain? = terrains[id]
}
