package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestPickUpDroppedItem
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.test.Test

class OnRequestPickUpDroppedItemTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onRequestPickUpDroppedItem = OnRequestPickUpDroppedItem(PickUpDroppedItem(lootDropRepository, eventBus), eventBus)

    @Test
    fun `should pick up the dropped item from its tile when its pick-up is requested`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(RequestPickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        lootDropRepository
            .searchByUnitId("bat")!!
            .toDto()
            .droppedItems
            .shouldBeEmpty()
    }
}
