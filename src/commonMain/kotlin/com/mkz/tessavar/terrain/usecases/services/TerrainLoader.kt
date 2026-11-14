package com.mkz.tessavar.terrain.usecases.services

import com.mkz.tessavar.terrain.domain.Terrain

interface TerrainLoader {
    suspend fun initResources() {}

    fun loadTerrains(): List<Terrain.Dto>
}
