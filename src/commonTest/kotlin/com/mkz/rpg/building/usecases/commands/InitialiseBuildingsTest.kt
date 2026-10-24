package com.mkz.rpg.building.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.building.adapters.storage.InMemoryBuildingRepository
import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.domain.BuildingError
import com.mkz.rpg.building.domain.BuildingEvent.BuildingPlaced
import com.mkz.rpg.building.domain.BuildingMother.building
import com.mkz.rpg.building.usecases.services.FakeBuildingLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class InitialiseBuildingsTest {
    private val queries = InMemoryQueries()
    private val buildingRepository = InMemoryBuildingRepository()
    private val eventBus = FakeEventBus()

    private fun initialiseBuildings(vararg buildings: Building.Dto) = InitialiseBuildings(FakeBuildingLoader(buildings.toList()), buildingRepository, queries.canBattlefieldTileBeOccupied, eventBus)()

    @Test
    fun `should place every loaded building on its tiles, requesting the battlefield to occupy them, when the buildings are initialised`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . .\n. . .\n. . ."))
        val fortress = building(id = "fortress", row = 0, column = 0, rows = 2, columns = 2).toDto()
        val tower = building(id = "tower", row = 2, column = 2).toDto()
        // When
        initialiseBuildings(fortress, tower)
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
        initialiseBuildings(building(id = "fortress", row = 0, column = 2).toDto())
        // Then
        buildingRepository.searchById("fortress")?.toDto()?.column shouldBe 0
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when any tile of the building is void`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . .\n. . #"))
        // When
        val result = runCatching { initialiseBuildings(building(id = "fortress", row = 0, column = 1, rows = 2, columns = 2).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingTileCanNotBeOccupied>()
        buildingRepository.searchById("fortress").shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when any tile of the building is out of the battlefield`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". .\n. ."))
        // When
        val result = runCatching { initialiseBuildings(building(id = "fortress", row = 1, column = 0, rows = 2, columns = 2).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingTileCanNotBeOccupied>()
        buildingRepository.searchById("fortress").shouldBeNull()
    }
}
