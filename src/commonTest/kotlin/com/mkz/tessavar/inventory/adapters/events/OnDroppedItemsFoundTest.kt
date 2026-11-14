package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.usecases.commands.StoreLootedItem
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnDroppedItemsFoundTest {
    private val eventBus = InMemoryEventBus()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val onDroppedItemsFound =
        OnDroppedItemsFound(
            storeLootedItem =
                StoreLootedItem(
                    inventoryRepository,
                    SearchLootableItemById(lootableItemRepository),
                    eventBus,
                ),
            eventBus = eventBus,
        )

    @Test
    fun `should store every item found in the inventory when a battle unit finds dropped items`() {
        // Given
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
        lootableItemRepository.create(consumableLootableItem(id = "antidote"))
        inventoryRepository.create(inventory(battleUnitId = "knight-1"))
        eventBus.publish(DroppedItemsFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion", "antidote"), row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.storedItems shouldBe listOf("health-potion", "antidote")
    }
}
