package com.mkz.tessavar.terrain.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.domain.TerrainError.TerrainAlreadyExists
import com.mkz.tessavar.terrain.domain.TerrainEvent
import com.mkz.tessavar.terrain.domain.TerrainRepository
import com.mkz.tessavar.terrain.usecases.services.TerrainLoader

class InitialiseTerrains(
    private val terrainLoader: TerrainLoader,
    private val terrainRepository: TerrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val terrains = terrainLoader.loadTerrains()
        val events = mutableSetOf<TerrainEvent>()
        terrains.forEach { terrainDto ->
            if (terrainRepository.searchById(terrainDto.id) != null) throw TerrainAlreadyExists()
            val (terrainEvents, terrain) = Terrain.create(terrainDto).pullEvents()
            terrainRepository.create(terrain)
            events += terrainEvents
        }
        eventBus.publish(events)
    }
}
