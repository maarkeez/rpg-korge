package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanStoreItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val canStoreItem = CanStoreItem(inventoryRepository)

    @Test
    fun `should allow storing when the battle unit has not looted any item`() {
        // Given
        val battleUnitId = InventoryMother.battleUnitId()
        // When
        val canStore = canStoreItem(battleUnitId = battleUnitId)
        // Then
        canStore.shouldBeTrue()
    }

    @Test
    fun `should allow storing when the inventory has space left`() {
        // Given
        val inventory = InventoryMother.inventory(storedItems = listOf("health-potion"))
        inventoryRepository.create(inventory)
        // When
        val canStore = canStoreItem(battleUnitId = inventory.toDto().battleUnitId)
        // Then
        canStore.shouldBeTrue()
    }

    @Test
    fun `should not allow storing when the inventory is full`() {
        // Given
        val inventory = InventoryMother.fullInventory()
        inventoryRepository.create(inventory)
        // When
        val canStore = canStoreItem(battleUnitId = inventory.toDto().battleUnitId)
        // Then
        canStore.shouldBeFalse()
    }
}
