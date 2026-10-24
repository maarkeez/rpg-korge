package com.mkz.rpg.building.domain

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class BuildingTest {
    private val fortress = Building.Dto(id = "fortress", row = 17, column = 13, rows = 4, columns = 4)

    @Test
    fun `Place should place the building on its tiles`() {
        // When
        val building = Building.place(fortress)
        // Then
        building.toDto() shouldBe fortress
    }

    @Test
    fun `Place should publish the building placed event, then request the battlefield to occupy its tiles, when the building is placed`() {
        // When
        val (events, _) = Building.place(fortress).pullEvents()
        // Then
        events.shouldContainExactly(
            BuildingEvent.BuildingPlaced(buildingId = "fortress", row = 17, column = 13),
            BattlefieldEvent.RequestOccupyTiles(occupantId = "fortress", row = 17, column = 13, rows = 4, columns = 4),
        )
    }

    @Test
    fun `PullEvents should clear the events once pulled`() {
        // Given
        val (_, building) = Building.place(fortress).pullEvents()
        // When
        val (events, _) = building.pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Place should fail when the building has no rows`() {
        // When
        val result = runCatching { Building.place(fortress.copy(rows = 0)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingSizeNotPositive>()
    }

    @Test
    fun `Place should fail when the building has no columns`() {
        // When
        val result = runCatching { Building.place(fortress.copy(columns = 0)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BuildingError.BuildingSizeNotPositive>()
    }
}
