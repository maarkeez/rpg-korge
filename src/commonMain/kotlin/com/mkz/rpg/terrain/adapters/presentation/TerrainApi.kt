package com.mkz.rpg.terrain.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.events.OnRequestTerrainCreation
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainRepository
import com.mkz.rpg.terrain.usecases.commands.RequestTerrainCreation
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class TerrainApi(
    eventBus: EventBus,
) {
    // Storage
    private val terrainRepository: TerrainRepository = InMemoryTerrainRepository()

    // Commands
    val requestTerrainCreation = RequestTerrainCreation(terrainRepository, eventBus)

    // Queries
    val searchTerrainById = SearchTerrainById(terrainRepository)

    // Events
    private val onRequestTerrainCreation = OnRequestTerrainCreation(requestTerrainCreation, eventBus)
}
