package com.mkz.rpg.terrain.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.events.OnRequestAllowedTransitionsInitialization
import com.mkz.rpg.terrain.adapters.events.OnRequestTerrainCreation
import com.mkz.rpg.terrain.adapters.resources.ResourceTerrainTransitionsService
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainRepository
import com.mkz.rpg.terrain.usecases.commands.InitialiseAllowedTransitions
import com.mkz.rpg.terrain.usecases.commands.RequestTerrainCreation
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.terrain.usecases.services.TerrainTransitionsService

class TerrainApi(
    eventBus: EventBus,
) {
    // Storage
    private val terrainRepository: TerrainRepository = InMemoryTerrainRepository()

    // Services
    private val terrainTransitionsService: TerrainTransitionsService = ResourceTerrainTransitionsService()

    // Commands
    val requestTerrainCreation = RequestTerrainCreation(terrainRepository, eventBus)
    val initialiseAllowedTransitions = InitialiseAllowedTransitions(terrainRepository, terrainTransitionsService)

    // Queries
    val searchTerrainById = SearchTerrainById(terrainRepository)
    val isTransitionAllowed = IsTransitionAllowed(terrainRepository)

    // Events
    private val onRequestTerrainCreation = OnRequestTerrainCreation(requestTerrainCreation, eventBus)
    private val onRequestAllowedTransitionsInitialization =
        OnRequestAllowedTransitionsInitialization(initialiseAllowedTransitions, eventBus)
}
