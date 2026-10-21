package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchDroppedItemsTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val searchDroppedItems = SearchDroppedItems(lootDropRepository)

    @Test
    fun `should return the items of each tile with dropped items, listed by the unit that dropped them, when items were dropped`() {
        // Given
        lootDropRepository.create(
            lootDrop(
                unitId = "bat",
                droppedItems = mapOf(PositionDto(row = 0, column = 0) to listOf("health-potion"), PositionDto(row = 0, column = 2) to listOf("health-potion", "helmet")),
            ),
        )
        lootDropRepository.create(lootDrop(unitId = "rat", droppedItems = mapOf(PositionDto(row = 0, column = 2) to listOf("hammer"))))
        // When
        val result = searchDroppedItems()
        // Then
        result shouldBe
            mapOf(
                PositionDto(row = 0, column = 0) to listOf("health-potion"),
                PositionDto(row = 0, column = 2) to listOf("health-potion", "helmet", "hammer"),
            )
    }

    @Test
    fun `should return no items when nothing was dropped`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat"))
        // When
        val result = searchDroppedItems()
        // Then
        result.shouldBeEmpty()
    }
}
