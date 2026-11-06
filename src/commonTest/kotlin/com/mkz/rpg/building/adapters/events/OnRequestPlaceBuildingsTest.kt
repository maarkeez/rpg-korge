package com.mkz.rpg.building.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.rpg.building.domain.BuildingEvent
import com.mkz.rpg.building.domain.BuildingMother.building
import com.mkz.rpg.building.usecases.commands.PlaceBuildings
import com.mkz.rpg.building.usecases.services.FakeBuildingLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestPlaceBuildingsTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val buildingRepository = InMemoryBuildingRepository()
    private val fortress = building(id = "fortress").toDto()
    private val onRequestPlaceBuildings =
        OnRequestPlaceBuildings(
            PlaceBuildings(FakeBuildingLoader(listOf(fortress)), buildingRepository, queries.isTileVacant, eventBus),
            eventBus,
        )

    @Test
    fun `should place the buildings when the request is dispatched`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(BuildingEvent.RequestPlaceBuildings)
        // When
        eventBus.dispatch()
        // Then
        buildingRepository.searchById("fortress")?.toDto() shouldBe fortress
    }
}
