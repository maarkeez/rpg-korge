package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.TerrainRepository

class HasTerrainTransition(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(
        terrainId: String,
        toTerrainId: String,
    ): Boolean {
        val terrain = terrainRepository.searchById(terrainId) ?: return false
        return terrain.hasTransitionTo(toTerrainId)
    }
}
