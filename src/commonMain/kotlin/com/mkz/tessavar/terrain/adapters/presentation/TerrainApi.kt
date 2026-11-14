package com.mkz.tessavar.terrain.adapters.presentation

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.terrain.adapters.events.OnRequestInitialiseTerrains
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainRepository
import com.mkz.tessavar.terrain.usecases.commands.InitialiseTerrains
import com.mkz.tessavar.terrain.usecases.queries.HasTerrainTransition
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrains
import com.mkz.tessavar.terrain.usecases.services.TerrainLoader

class TerrainApi(
    eventBus: EventBus,
    terrainLoader: TerrainLoader,
) {
    private val terrainRepository: TerrainRepository = InMemoryTerrainRepository()

    private val terrainLoader = terrainLoader

    val initialiseTerrains = InitialiseTerrains(terrainLoader, terrainRepository, eventBus)

    val searchTerrainById = SearchTerrainById(terrainRepository)
    val hasTerrainTransition = HasTerrainTransition(terrainRepository)
    val searchTerrains = SearchTerrains(terrainRepository)

    private val onRequestInitialiseTerrains = OnRequestInitialiseTerrains(initialiseTerrains, eventBus)

    suspend fun init() {
        terrainLoader.initResources()
    }
}
