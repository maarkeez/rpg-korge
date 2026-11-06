package com.mkz.rpg.building.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.building.adapters.events.OnRequestPlaceBuildings
import com.mkz.rpg.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.rpg.building.domain.BuildingRepository
import com.mkz.rpg.building.usecases.commands.PlaceBuildings
import com.mkz.rpg.building.usecases.services.BuildingLoader
import com.mkz.rpg.shared.domain.EventBus

class BuildingApi(
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val buildingLoader: BuildingLoader,
) {
    // Storage
    private val buildingRepository: BuildingRepository = InMemoryBuildingRepository()

    // Commands
    val placeBuildings = PlaceBuildings(buildingLoader, buildingRepository, battlefieldApi.isTileVacant, eventBus)

    // Events
    private val onRequestPlaceBuildings = OnRequestPlaceBuildings(placeBuildings, eventBus)

    suspend fun init() {
        buildingLoader.initResources()
    }
}
