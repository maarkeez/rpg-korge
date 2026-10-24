package com.mkz.rpg.building.usecases.services

import com.mkz.rpg.building.domain.Building

interface BuildingLoader {
    suspend fun initResources() {}

    fun loadBuildings(): List<Building.Dto>
}
