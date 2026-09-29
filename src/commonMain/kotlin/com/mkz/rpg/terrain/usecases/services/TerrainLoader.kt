package com.mkz.rpg.terrain.usecases.services

import com.mkz.rpg.terrain.domain.Terrain

interface TerrainLoader {
    fun loadTerrains(): List<Terrain.Dto>
}
