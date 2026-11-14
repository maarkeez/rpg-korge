package com.mkz.tessavar.building.adapters.presentation

import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.building.adapters.events.OnRequestPlaceBuildings
import com.mkz.tessavar.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.tessavar.building.domain.BuildingRepository
import com.mkz.tessavar.building.usecases.commands.PlaceBuildings
import com.mkz.tessavar.building.usecases.services.BuildingLoader
import com.mkz.tessavar.shared.domain.EventBus

class BuildingApi(
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val buildingLoader: BuildingLoader,
) {
    private val buildingRepository: BuildingRepository = InMemoryBuildingRepository()

    val placeBuildings = PlaceBuildings(buildingLoader, buildingRepository, battlefieldApi.isTileVacant, eventBus)

    private val onRequestPlaceBuildings = OnRequestPlaceBuildings(placeBuildings, eventBus)

    suspend fun init() {
        buildingLoader.initResources()
    }
}
