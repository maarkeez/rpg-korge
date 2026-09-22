package com.mkz.rpg.terrain.domain

interface TerrainRepository {
    fun create(terrain: Terrain)

    fun searchById(id: String): Terrain?
}
