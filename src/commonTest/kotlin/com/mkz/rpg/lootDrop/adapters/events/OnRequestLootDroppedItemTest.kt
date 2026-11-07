package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestLootDroppedItem
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.test.Test

class OnRequestLootDroppedItemTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onRequestLootDroppedItem = OnRequestLootDroppedItem(LootDroppedItem(lootDropRepository, eventBus), eventBus)

    @Test
    fun `should take the dropped item off its tile when its looting is requested`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(RequestLootDroppedItem(lootableItemId = "health-potion", row = 0, column = 1))
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
