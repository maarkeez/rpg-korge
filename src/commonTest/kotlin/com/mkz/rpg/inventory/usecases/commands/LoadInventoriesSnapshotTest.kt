package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class LoadInventoriesSnapshotTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val loadInventoriesSnapshot =
        LoadInventoriesSnapshot(
            inventoryRepository,
            SearchLootableItemById(
                InMemoryLootableItemRepository().apply {
                    listOf("health-potion", "iron-helmet").forEach { id ->
                        create(lootableItem(id = id))
                    }
                },
            ),
        )

    @Test
    fun `should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val inventories =
            listOf(
                inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "iron-helmet"), equippedItems = mapOf(EquipmentZoneDto.HEAD to "iron-helmet"), gold = 40),
                inventory(battleUnitId = "mage-1"),
            )
        inventoryRepository.create(inventory(battleUnitId = "rat-1", gold = 5))
        // When
        loadInventoriesSnapshot(inventories.map { inventory -> inventory.toDto() })
        // Then
        inventoryRepository.searchAll().shouldContainExactlyInAnyOrder(inventories)
    }

    @Test
    fun `should fail when an item is a lootable item the game no longer has`() {
        // When
        val result = runCatching { loadInventoriesSnapshot(listOf(inventory(storedItems = listOf("old-sword")).toDto())) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-sword"
    }
}
