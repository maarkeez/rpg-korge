package com.mkz.rpg.building.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.building.adapters.events.OnRequestInitialiseBuildings
import com.mkz.rpg.building.adapters.resources.ResourcesBuildingLoader
import com.mkz.rpg.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.rpg.building.domain.BuildingRepository
import com.mkz.rpg.building.usecases.commands.InitialiseBuildings
import com.mkz.rpg.building.usecases.services.BuildingLoader
import com.mkz.rpg.shared.domain.EventBus

class BuildingApi(
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val buildingLoader: BuildingLoader = ResourcesBuildingLoader(),
) {
    // Storage
    private val buildingRepository: BuildingRepository = InMemoryBuildingRepository()

    // Commands
    val initialiseBuildings = InitialiseBuildings(buildingLoader, buildingRepository, battlefieldApi.canBattlefieldTileBeOccupied, eventBus)

    // Events
    private val onRequestInitialiseBuildings = OnRequestInitialiseBuildings(initialiseBuildings, eventBus)

    suspend fun init() {
        buildingLoader.initResources()
    }
}
