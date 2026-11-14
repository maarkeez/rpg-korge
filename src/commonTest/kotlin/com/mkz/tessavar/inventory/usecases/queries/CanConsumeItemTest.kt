package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.learnAbilityEffect
import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.usecases.services.ConsumptionRule
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
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
            ConsumptionRule(queries.searchEffectById, queries.canMemberLearnAbility),
        )

    @BeforeTest
    fun setUp() {
        queries.effectRepository.create(learnAbilityEffect(id = "learn-teleport", abilityId = "teleport"))
        queries.effectRepository.create(healEffect(id = "heal"))
        queries.lootableItemRepository.create(consumableLootableItem(id = "teleport-essence", consumable = consumable(effectIds = setOf("learn-teleport"))))
        queries.lootableItemRepository.create(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal"))))
        queries.lootableItemRepository.create(equippableLootableItem(id = "hammer"))
        queries.battleUnitRepository.create(battleUnit(id = "viking-1", unit = unit(startingAbilities = listOf("sword")).toDto()))
        queries.partyRepository.save(party(battleUnitIds = listOf("viking-1", "knight-1")))
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
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("sword", "teleport")).toDto()))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "knight-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should not allow consuming when the battle unit already knows 6 abilities`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(startingAbilities = List(6) { index -> "ability-$index" }).toDto()))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "knight-1", lootableItemId = "teleport-essence")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should not allow consuming a consumable that teaches an ability when the battle unit is no party member`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(startingAbilities = emptyList()).toDto()))
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("teleport-essence")))
        // When
        val result = canConsumeItem(battleUnitId = "rat-1", lootableItemId = "teleport-essence")
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
