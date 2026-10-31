package com.mkz.rpg.inventory.adapters.save

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.commands.RestoreInventories
import com.mkz.rpg.inventory.usecases.queries.SearchInventoriesSnapshot
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventorySnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { listOf("health-potion", "iron-helmet").forEach { id -> create(lootableItem(id = id)) } })

    // The knight carries a health potion and wears an iron helmet, with 40 gold. The mage carries nothing.
    private val inventories =
        listOf(
            inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "iron-helmet"), equippedItems = mapOf(EquipmentZoneDto.HEAD to "iron-helmet"), gold = 40),
            inventory(battleUnitId = "mage-1"),
        )

    private fun snapshottable(repository: InventoryRepository) = InventorySnapshottable(SearchInventoriesSnapshot(repository), RestoreInventories(repository, searchLootableItemById))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryInventoryRepository().apply { inventories.forEach(::create) }).snapshot()
        val restoredRepository = InMemoryInventoryRepository()
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(inventories)
    }

    @Test
    fun `Key should be inventory`() {
        // When
        val result = snapshottable(InMemoryInventoryRepository()).key
        // Then
        result shouldBe "inventory"
    }
}
