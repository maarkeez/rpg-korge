package com.mkz.tessavar.building.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.tessavar.building.domain.Building
import com.mkz.tessavar.building.domain.BuildingError
import com.mkz.tessavar.building.domain.BuildingEvent.BuildingPlaced
import com.mkz.tessavar.building.domain.BuildingMother.building
import com.mkz.tessavar.building.usecases.services.FakeBuildingLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class PlaceBuildingsTest {
    private val queries = InMemoryQueries()
    private val buildingRepository = InMemoryBuildingRepository()
    private val eventBus = FakeEventBus()

    private fun placeBuildings(vararg buildings: Building.Dto) = PlaceBuildings(FakeBuildingLoader(buildings.toList()), buildingRepository, queries.isTileVacant, eventBus)()

    @Test
    fun `should place every loaded building on its tiles, requesting the battlefield to occupy them, when the buildings are placed`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . .\n. . .\n. . ."))
        val fortress = building(id = "fortress", row = 0, column = 0, rows = 2, columns = 2).toDto()
        val tower = building(id = "tower", row = 2, column = 2).toDto()
        // When
        placeBuildings(fortress, tower)
        // Then
        buildingRepository.searchById("fortress")?.toDto() shouldBe fortress
        buildingRepository.searchById("tower")?.toDto() shouldBe tower
        eventBus.shouldHavePublishedEvents(
            BuildingPlaced(buildingId = "fortress", row = 0, column = 0),
            RequestOccupyTiles(occupantId = "fortress", row = 0, column = 0, rows = 2, columns = 2),
            BuildingPlaced(buildingId = "tower", row = 2, column = 2),
            RequestOccupyTiles(occupantId = "tower", row = 2, column = 2, rows = 1, columns = 1),
        )
    }

    @Test
    fun `should not place a building again when it is already placed`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . ."))
        buildingRepository.save(building(id = "fortress", row = 0, column = 0))
        // When
        placeBuildings(building(id = "fortress", row = 0, column = 2).toDto())
        // Then
        buildingRepository.searchById("fortress")?.toDto()?.column shouldBe 0
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when any tile of the building is void`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . .\n. . #"))
        // When
        val result = runCatching { placeBuildings(building(id = "fortress", row = 0, column = 1, rows = 2, columns = 2).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingTileNotVacant>()
        buildingRepository.searchById("fortress").shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when any tile of the building is out of the battlefield`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". .\n. ."))
        // When
        val result = runCatching { placeBuildings(building(id = "fortress", row = 1, column = 0, rows = 2, columns = 2).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingTileNotVacant>()
        buildingRepository.searchById("fortress").shouldBeNull()
    }
}
