package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchInventoriesSnapshotTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val searchInventoriesSnapshot = SearchInventoriesSnapshot(inventoryRepository)

    @Test
    fun `should return the items and the gold of every inventory`() {
        // Given
        val inventories =
            listOf(
                inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "iron-helmet"), equippedItems = mapOf(EquipmentZoneDto.HEAD to "iron-helmet"), gold = 40),
                inventory(battleUnitId = "mage-1"),
            )
        inventories.forEach(inventoryRepository::create)
        // When
        val result = searchInventoriesSnapshot()
        // Then
        result shouldContainExactlyInAnyOrder inventories.map { inventory -> inventory.toDto() }
    }

    @Test
    fun `should return nothing when there is no inventory`() {
        // When
        val result = searchInventoriesSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
