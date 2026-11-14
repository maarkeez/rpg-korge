package com.mkz.tessavar.terrain.domain

interface TerrainRepository {
    fun create(terrain: Terrain)

    fun update(terrain: Terrain)

    fun searchById(id: String): Terrain?

    fun searchAll(): List<Terrain>
}
