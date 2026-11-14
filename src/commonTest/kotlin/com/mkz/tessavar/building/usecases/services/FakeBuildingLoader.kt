package com.mkz.tessavar.building.usecases.services

import com.mkz.tessavar.building.domain.Building

class FakeBuildingLoader(
    private val buildings: List<Building.Dto> = emptyList(),
) : BuildingLoader {
    override fun loadBuildings(): List<Building.Dto> = buildings
}
