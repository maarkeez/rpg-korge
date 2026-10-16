package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainRepository

/** Every terrain, in id order, e.g.: to choose the terrain of a tile while designing a map. */
class SearchTerrains(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(): List<Terrain.Dto> = terrainRepository.searchAll().map(Terrain::toDto).sortedBy(Terrain.Dto::id)
}
