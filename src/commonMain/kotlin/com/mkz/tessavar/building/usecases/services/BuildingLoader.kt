package com.mkz.tessavar.building.usecases.services

import com.mkz.tessavar.building.domain.Building

interface BuildingLoader {
    suspend fun initResources() {}

    fun loadBuildings(): List<Building.Dto>
}
