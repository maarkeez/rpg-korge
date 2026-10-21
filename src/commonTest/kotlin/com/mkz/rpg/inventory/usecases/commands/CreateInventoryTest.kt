package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateInventoryTest {
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val createInventory = CreateInventory(inventoryRepository, queries.searchUnitById)

    @BeforeTest
    fun setUp() {
        queries.unitRepository.create(unit(id = "viking", startingGold = 100))
    }

    @Test
    fun `should create the empty inventory of a party member, with the starting gold of its unit`() {
        // When
        createInventory(battleUnitId = "viking-1", unitId = "viking")
        // Then
        val inventory = inventoryRepository.searchByBattleUnitId("viking-1")!!.toDto()
        inventory.storedItems shouldBe emptyList()
        inventory.gold shouldBe 100
    }

    @Test
    fun `should keep the inventory as it is when the battle unit already has one`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer"), gold = 3))
        // When
        createInventory(battleUnitId = "viking-1", unitId = "viking")
        // Then
        inventoryRepository.searchByBattleUnitId("viking-1")!!.toDto().gold shouldBe 3
    }

    @Test
    fun `should create the inventory without gold when the unit does not exist`() {
        // When
        createInventory(battleUnitId = "rat-1", unitId = "missing-unit")
        // Then
        inventoryRepository.searchByBattleUnitId("rat-1")!!.toDto().gold shouldBe 0
    }
}
