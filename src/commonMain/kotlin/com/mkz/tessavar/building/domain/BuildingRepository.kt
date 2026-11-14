package com.mkz.tessavar.building.domain

interface BuildingRepository {
    fun save(building: Building)

    fun searchById(id: String): Building?

    fun searchAll(): List<Building>
}
