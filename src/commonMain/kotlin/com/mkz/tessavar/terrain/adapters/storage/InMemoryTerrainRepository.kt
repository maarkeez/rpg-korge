package com.mkz.tessavar.terrain.adapters.storage

import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.domain.TerrainRepository

class InMemoryTerrainRepository : TerrainRepository {
    private val terrains = mutableMapOf<String, Terrain>()

    override fun create(terrain: Terrain) {
        terrains[terrain.toDto().id] = terrain
    }

    override fun update(terrain: Terrain) {
        terrains[terrain.toDto().id] = terrain
    }

    override fun searchById(id: String): Terrain? = terrains[id]

    override fun searchAll(): List<Terrain> = terrains.values.toList()
}
