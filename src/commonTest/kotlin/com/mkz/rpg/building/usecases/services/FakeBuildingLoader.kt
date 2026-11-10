package com.mkz.rpg.building.usecases.services

import com.mkz.rpg.building.domain.Building

class FakeBuildingLoader(
    private val buildings: List<Building.Dto> = emptyList(),
) : BuildingLoader {
    override fun loadBuildings(): List<Building.Dto> = buildings
}
