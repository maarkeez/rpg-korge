package com.mkz.tessavar.terrain.usecases.queries

import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.domain.TerrainRepository

class SearchTerrains(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(): List<Terrain.Dto> = terrainRepository.searchAll().map(Terrain::toDto).sortedBy(Terrain.Dto::id)
}
