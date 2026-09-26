package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.domain.TerrainRepository

class IsTransitionAllowed(
    private val terrainRepository: TerrainRepository,
) {
    operator fun invoke(
        terrainId: String,
        transitionToTerrainId: String,
    ): Boolean {
        val terrain = terrainRepository.searchById(terrainId) ?: return false
        return terrain.canTransitionTo(transitionToTerrainId)
    }
}
