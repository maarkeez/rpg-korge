package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class UnequipItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val partyRepository = InMemoryPartyRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()
    private val unequipItem =
        UnequipItem(
            inventoryRepository,
            SearchLootableItemById(lootableItemRepository),
            PlayerTurnRule(SearchParty(partyRepository), SearchBattle(battleRepository)),
            eventBus,
        )

    // It is the turn of the human player, who explores with the knight. The rat is no party member.
    @BeforeTest
    fun setUp() {
        battleRepository.create(battle(players = listOf("human", "cpu")))
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
        lootableItemRepository.create(equippableLootableItem(id = "hammer", equippable = equippable(effectIds = setOf("maximum-health-20"), equipmentZone = WEAPON)))
    }

    @Test
    fun `should unequip the item and request the removal of its effects from its battle unit when the item is equipped`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // When
        unequipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        val inventory = inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()
        inventory?.equippedItems shouldBe emptyMap()
        inventory?.storedItems?.shouldContainExactly("hammer")
        eventBus.shouldHavePublishedEvents(
            ItemUnequipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON),
            RequestRemoveMemberEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"),
        )
    }

    @Test
    fun `should fail when the item is not equipped`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer")))
        // When
        val unequipping = { unequipItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<ItemNotEquipped> { unequipping() }
    }

    @Test
    fun `should fail when the battle unit has not looted any item`() {
        // Given
        // When
        val unequipping = { unequipItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<InventoryItemNotFound> { unequipping() }
    }

    @Test
    fun `should fail when it is not the turn of the party player`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human")))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // When
        val unequipping = { unequipItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { unequipping() }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // When
        val unequipping = { unequipItem(battleUnitId = "rat-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<NotAPartyMember> { unequipping() }
    }
}
