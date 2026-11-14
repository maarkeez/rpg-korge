package com.mkz.tessavar.inventory.adapters.save

import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.commands.LoadInventoriesSnapshot
import com.mkz.tessavar.inventory.usecases.queries.SearchInventoriesSnapshot
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventorySnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { listOf("health-potion", "iron-helmet").forEach { id -> create(lootableItem(id = id)) } })

    private val inventories =
        listOf(
            inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "iron-helmet"), equippedItems = mapOf(EquipmentZoneDto.HEAD to "iron-helmet"), gold = 40),
            inventory(battleUnitId = "mage-1"),
        )

    private fun snapshottable(repository: InventoryRepository) = InventorySnapshottable(SearchInventoriesSnapshot(repository), LoadInventoriesSnapshot(repository, searchLootableItemById))

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryInventoryRepository().apply { inventories.forEach(::create) }).snapshot()
        val loadedRepository = InMemoryInventoryRepository()
        // When
        snapshottable(loadedRepository).load(snapshot)
        // Then
        loadedRepository.searchAll().shouldContainExactlyInAnyOrder(inventories)
    }

    @Test
    fun `Key should be inventory`() {
        // When
        val result = snapshottable(InMemoryInventoryRepository()).key
        // Then
        result shouldBe "inventory"
    }
}
