package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotStored
import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.tessavar.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.usecases.services.PlayerTurnRule
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
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
        shouldThrow<ItemNotStored> { unequipping() }
    }

    @Test
    fun `should fail when it is not the player turn of the human player`() {
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
