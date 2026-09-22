package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainAlreadyExists
import com.mkz.rpg.terrain.domain.TerrainRepository

class RequestTerrainCreation(
    private val terrainRepository: TerrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(terrain: Terrain.Dto) {
        val storedTerrain = terrainRepository.searchById(terrain.id)
        if (storedTerrain != null) throw TerrainAlreadyExists()
        val (events, terrain) = Terrain.create(terrain).pullEvents()
        terrainRepository.create(terrain)
        eventBus.publish(events)
    }
}
