package com.mkz.rpg.terrain.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.events.OnRequestInitialiseTerrains
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainRepository
import com.mkz.rpg.terrain.usecases.commands.InitialiseTerrains
import com.mkz.rpg.terrain.usecases.queries.HasTerrainTransition
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.terrain.usecases.queries.SearchTerrains
import com.mkz.rpg.terrain.usecases.services.TerrainLoader

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
