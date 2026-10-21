package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootFound
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnLootFoundTest {
    private val eventBus = InMemoryEventBus()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val onLootFound =
        OnLootFound(
            storeLootedItem =
                StoreLootedItem(
                    inventoryRepository,
                    SearchLootableItemById(lootableItemRepository),
                    eventBus,
                ),
            eventBus = eventBus,
        )

    @Test
    fun `should store every item found in the inventory when a battle unit finds loot`() {
        // Given
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
        lootableItemRepository.create(consumableLootableItem(id = "antidote"))
        inventoryRepository.create(inventory(battleUnitId = "knight-1"))
        eventBus.publish(LootFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion", "antidote"), row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.storedItems shouldBe listOf("health-potion", "antidote")
    }
}
