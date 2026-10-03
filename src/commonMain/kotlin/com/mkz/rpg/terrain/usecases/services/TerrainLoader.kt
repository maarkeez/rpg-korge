package com.mkz.rpg.terrain.usecases.services

import com.mkz.rpg.terrain.domain.Terrain

interface TerrainLoader {
    suspend fun initResources() {}

    fun loadTerrains(): List<Terrain.Dto>
}
