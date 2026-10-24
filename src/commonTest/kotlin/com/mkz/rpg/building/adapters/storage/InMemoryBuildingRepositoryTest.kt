package com.mkz.rpg.building.adapters.storage

import com.mkz.rpg.building.domain.BuildingMother.building
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryBuildingRepositoryTest {
    private val buildingRepository = InMemoryBuildingRepository()

    @Test
    fun `Save should store the building`() {
        // Given
        val building = building(id = "fortress")
        // When
        buildingRepository.save(building)
        // Then
        buildingRepository.searchById("fortress") shouldBe building
    }

    @Test
    fun `SearchById should return null when the building is not stored`() {
        // When
        val building = buildingRepository.searchById("unknown")
        // Then
        building.shouldBeNull()
    }

    @Test
    fun `SearchAll should return every stored building`() {
        // Given
        val fortress = building(id = "fortress")
        val tower = building(id = "tower")
        buildingRepository.save(fortress)
        buildingRepository.save(tower)
        // When
        val buildings = buildingRepository.searchAll()
        // Then
        buildings.shouldContainExactlyInAnyOrder(fortress, tower)
    }
}
