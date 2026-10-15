package com.mkz.rpg.inventory.adapters.storage

import com.mkz.rpg.inventory.domain.InventoryMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryInventoryRepositoryTest {
    private val inventoryRepository = InMemoryInventoryRepository()

    @Test
    fun `Create should store the inventory when the inventory is created`() {
        // Given
        val inventory = InventoryMother.inventory(storedItems = listOf("health-potion"))
        // When
        inventoryRepository.create(inventory)
        // Then
        val storedInventory = inventoryRepository.searchByBattleUnitId(inventory.toDto().battleUnitId)
        storedInventory shouldBe inventory
    }

    @Test
    fun `SearchByBattleUnitId should return the inventory when the inventory of the battle unit is stored`() {
        // Given
        val inventory = InventoryMother.inventory(storedItems = listOf("health-potion"))
        inventoryRepository.create(inventory)
        // When
        val storedInventory = inventoryRepository.searchByBattleUnitId(inventory.toDto().battleUnitId)
        // Then
        storedInventory shouldBe inventory
    }

    @Test
    fun `SearchByBattleUnitId should return null when the battle unit has no inventory`() {
        // Given
        val battleUnitId = InventoryMother.battleUnitId()
        // When
        val storedInventory = inventoryRepository.searchByBattleUnitId(battleUnitId)
        // Then
        storedInventory.shouldBeNull()
    }
}
