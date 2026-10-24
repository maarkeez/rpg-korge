package com.mkz.rpg.building.adapters.storage

import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.domain.BuildingRepository

class InMemoryBuildingRepository : BuildingRepository {
    private val buildings = mutableMapOf<String, Building>()

    override fun save(building: Building) {
        buildings[building.toDto().id] = building
    }

    override fun searchById(id: String) = buildings[id]

    override fun searchAll() = buildings.values.toList()
}
