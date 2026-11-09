package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainRepository

/** Every terrain, in id order, e.g.: to select the terrain to paint with in the world builder. */
class SearchTerrains(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(): List<Terrain.Dto> = terrainRepository.searchAll().map(Terrain::toDto).sortedBy(Terrain.Dto::id)
}
