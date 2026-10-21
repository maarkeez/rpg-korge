package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceDroppedItemTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val placeDroppedItem = PlaceDroppedItem(lootDropRepository, eventBus)

    @Test
    fun `should place the item on its tile when the unit has a loot drop`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat"))
        // When
        placeDroppedItem(unitId = "bat", lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(LootDropped(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `should not place the item when the unit has no loot drop`() {
        // Given
        // When
        placeDroppedItem(unitId = "bat", lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
