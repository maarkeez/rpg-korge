package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.learnAbilityEffect
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.ConsumptionRule
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

class CanConsumeItemTest {
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val canConsumeItem =
        CanConsumeItem(
            inventoryRepository,
            queries.searchLootableItemById,
            ConsumptionRule(queries.searchEffectById, queries.canLearnAbility),
        )

    // The teleport essence teaches teleport, and the health potion heals. The viking only knows sword.
    @BeforeTest
    fun setUp() {
        queries.effectRepository.create(learnAbilityEffect(id = "learn-teleport", abilityId = "teleport"))
        queries.effectRepository.create(increaseHealthEffect(id = "heal"))
        queries.lootableItemRepository.create(consumableLootableItem(id = "teleport-essence", consumable = consumable(effectIds = setOf("learn-teleport"))))
        queries.lootableItemRepository.create(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal"))))
        queries.lootableItemRepository.create(equippableLootableItem(id = "hammer"))
        queries.battleUnitRepository.create(battleUnit(id = "viking-1", unit = unit(abilities = listOf("sword")).toDto()))
    }

    @Test
    fun `should allow consuming a stored consumable that teaches nothing`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("health-potion")))
        // When
        val result = canConsumeItem(battleUnitId = "viking-1", lootableItemId = "health-potion")
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should allow consuming a stored consumable when the battle unit can learn the ability it teaches`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "viking-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should not allow consuming when the battle unit already knows the ability it teaches`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("sword", "teleport")).toDto()))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "knight-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should not allow consuming when the battle unit already knows 6 abilities`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = List(6) { index -> "ability-$index" }).toDto()))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "knight-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should not allow consuming when the item is not stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("health-potion")))
        // When
        val result = canConsumeItem(battleUnitId = "viking-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should not allow consuming when the item is not consumable`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer")))
        // When
        val result = canConsumeItem(battleUnitId = "viking-1", lootableItemId = "hammer")
        // Then
        result.shouldBeFalse()
    }
}
