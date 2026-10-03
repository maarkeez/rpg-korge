package com.mkz.rpg.terrain.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.events.OnRequestInitialiseTerrains
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainRepository
import com.mkz.rpg.terrain.usecases.commands.InitialiseTerrains
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.terrain.usecases.services.TerrainLoader

class TerrainApi(
    eventBus: EventBus,
    terrainLoader: TerrainLoader = ResourcesTerrainLoader(),
) {
    // Storage
    private val terrainRepository: TerrainRepository = InMemoryTerrainRepository()

    // Services
    private val terrainLoader = terrainLoader

    // Commands
    val initialiseTerrains = InitialiseTerrains(terrainLoader, terrainRepository, eventBus)

    // Queries
    val searchTerrainById = SearchTerrainById(terrainRepository)
    val isTransitionAllowed = IsTransitionAllowed(terrainRepository)

    // Events
    private val onRequestInitialiseTerrains = OnRequestInitialiseTerrains(initialiseTerrains, eventBus)

    suspend fun init() {
        terrainLoader.initResources()
    }
}
