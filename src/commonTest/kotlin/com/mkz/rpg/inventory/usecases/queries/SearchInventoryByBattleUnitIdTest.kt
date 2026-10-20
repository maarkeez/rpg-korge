package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchInventoryByBattleUnitIdTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val searchInventoryByBattleUnitId = SearchInventoryByBattleUnitId(inventoryRepository)

    @Test
    fun `should find the inventory when the battle unit has looted an item`() {
        // Given
        val inventory = InventoryMother.inventory(storedItems = listOf("health-potion"))
        inventoryRepository.create(inventory)
        // When
        val inventoryFound = searchInventoryByBattleUnitId(battleUnitId = inventory.toDto().battleUnitId)
        // Then
        inventoryFound shouldBe inventory.toDto()
    }

    @Test
    fun `should not find an inventory when the battle unit has none`() {
        // Given
        val battleUnitId = InventoryMother.battleUnitId()
        // When
        val inventoryFound = searchInventoryByBattleUnitId(battleUnitId = battleUnitId)
        // Then
        inventoryFound.shouldBeNull()
    }
}
