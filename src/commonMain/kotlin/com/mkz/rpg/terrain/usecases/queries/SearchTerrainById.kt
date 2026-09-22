package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainRepository

class SearchTerrainById(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(terrainId: String): Terrain.Dto? = terrainRepository.searchById(terrainId)?.toDto()
}
