package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.test.Test

class OnItemStoredTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onItemStored = OnItemStored(PickUpDroppedItem(battlefieldRepository, eventBus), eventBus)

    @Test
    fun `should pick up the dropped item from its tile when an inventory stores it`() {
        // Given
        battlefieldRepository.create(battlefield(". .", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(ItemStored(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.droppedItems()?.shouldBeEmpty()
    }
}
