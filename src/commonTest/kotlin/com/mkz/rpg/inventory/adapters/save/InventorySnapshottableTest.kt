package com.mkz.rpg.inventory.adapters.save

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class InventorySnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { listOf("health-potion", "iron-helmet").forEach { id -> create(lootableItem(id = id)) } })

    // The knight carries a health potion and wears an iron helmet, with 40 gold. The mage carries nothing.
    private val inventories =
        listOf(
            inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "iron-helmet"), equippedItems = mapOf(EquipmentZoneDto.HEAD to "iron-helmet"), gold = 40),
            inventory(battleUnitId = "mage-1"),
        )

    private fun snapshot(): String = InventorySnapshottable(InMemoryInventoryRepository().apply { inventories.forEach(::create) }, searchLootableItemById).snapshot()

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val restoredRepository = InMemoryInventoryRepository().apply { create(inventory(battleUnitId = "rat-1", gold = 5)) }
        // When
        InventorySnapshottable(restoredRepository, searchLootableItemById).restore(snapshot())
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(inventories)
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val restoredRepository = InMemoryInventoryRepository()
        // When
        InventorySnapshottable(restoredRepository, searchLootableItemById).restore(snapshot())
        // Then
        restoredRepository.searchAll().flatMap { inventory -> inventory.pullEvents().first }.shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when an item is a lootable item the game no longer has`() {
        // Given
        val snapshot = InventorySnapshottable(InMemoryInventoryRepository().apply { create(inventory(storedItems = listOf("old-sword"))) }, searchLootableItemById).snapshot()
        // When
        val result = runCatching { InventorySnapshottable(InMemoryInventoryRepository(), searchLootableItemById).restore(snapshot) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-sword"
    }
}
