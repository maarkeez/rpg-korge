package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import com.mkz.rpg.terrain.domain.TerrainRepository
import com.mkz.rpg.terrain.usecases.services.TerrainTransitionsService

class InitialiseAllowedTransitions(
    private val terrainRepository: TerrainRepository,
    private val terrainTransitionsService: TerrainTransitionsService,
) {
    operator fun invoke() {
        val transitions = terrainTransitionsService.searchTransitions()
        val referencedTerrainIds = (transitions.keys + transitions.values.flatten()).distinct()
        referencedTerrainIds.forEach { terrainId ->
            if (terrainRepository.searchById(terrainId) == null) throw TerrainNotFound(terrainId)
        }
        transitions.forEach { (terrainId, allowedTransitions) ->
            allowedTransitions.forEach { transitionToTerrainId ->
                updateAllowedTransition(terrainId, transitionToTerrainId)
                updateAllowedTransition(transitionToTerrainId, terrainId)
            }
        }
        (transitions.keys + transitions.values.flatten().distinct()).forEach { terrainId ->
            updateAllowedTransition(terrainId, terrainId)
        }
    }

    private fun updateAllowedTransition(
        terrainId: String,
        allowedTransition: String,
    ) {
        val terrain = terrainRepository.searchById(terrainId) ?: return
        terrainRepository.update(terrain.addAllowedTransition(allowedTransition))
    }
}
