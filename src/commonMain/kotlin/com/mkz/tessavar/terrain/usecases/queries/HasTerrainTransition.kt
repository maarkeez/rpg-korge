package com.mkz.tessavar.terrain.usecases.queries

import com.mkz.tessavar.terrain.domain.TerrainRepository

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
