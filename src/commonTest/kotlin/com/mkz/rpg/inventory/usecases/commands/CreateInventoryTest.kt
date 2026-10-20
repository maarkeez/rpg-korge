package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateInventoryTest {
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val createInventory = CreateInventory(inventoryRepository, queries.searchBattleUnitById, queries.searchPlayerById, queries.searchUnitById)

    // The viking and the summoned bee belong to the human player, and the rat to the CPU player.
    @BeforeTest
    fun setUp() {
        val human = player(id = "human", type = HUMAN)
        val cpu = player(id = "cpu", type = CPU)
        val viking = unit(id = "viking", startingGold = 100)
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.unitRepository.create(viking)
        queries.battleUnitRepository.create(battleUnit(id = "viking-1", unit = viking.toDto(), player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(startingGold = 5).toDto(), player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "bee-1", unit = unit(startingGold = 5).toDto(), player = human.toDto(), remainingTurns = 5))
    }

    @Test
    fun `should create the empty inventory of a battle unit of the human player, with the starting gold of its unit`() {
        // When
        createInventory(battleUnitId = "viking-1")
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
        createInventory(battleUnitId = "viking-1")
        // Then
        inventoryRepository.searchByBattleUnitId("viking-1")!!.toDto().gold shouldBe 3
    }

    @Test
    fun `should not create an inventory for a battle unit of the CPU player`() {
        // When
        createInventory(battleUnitId = "rat-1")
        // Then
        inventoryRepository.searchByBattleUnitId("rat-1").shouldBeNull()
    }

    @Test
    fun `should not create an inventory for a summoned battle unit`() {
        // When
        createInventory(battleUnitId = "bee-1")
        // Then
        inventoryRepository.searchByBattleUnitId("bee-1").shouldBeNull()
    }

    @Test
    fun `should fail when the battle unit does not exist`() {
        // Then
        shouldThrow<InventoryBattleUnitDoesNotExist> { createInventory(battleUnitId = "missing-battle-unit") }
    }
}
