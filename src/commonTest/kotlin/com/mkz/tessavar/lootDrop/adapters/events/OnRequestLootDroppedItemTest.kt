package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.RequestLootDroppedItem
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
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
