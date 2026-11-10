package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainRepository

class SearchTerrains(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(): List<Terrain.Dto> = terrainRepository.searchAll().map(Terrain::toDto).sortedBy(Terrain.Dto::id)
}
