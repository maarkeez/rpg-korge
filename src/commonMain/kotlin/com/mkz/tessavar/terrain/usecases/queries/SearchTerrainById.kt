package com.mkz.tessavar.terrain.usecases.queries

import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.domain.TerrainRepository

class SearchTerrainById(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(terrainId: String): Terrain.Dto? = terrainRepository.searchById(terrainId)?.toDto()
}
