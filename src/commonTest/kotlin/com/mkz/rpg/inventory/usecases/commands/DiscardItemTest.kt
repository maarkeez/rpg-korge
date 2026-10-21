package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class DiscardItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val partyRepository = InMemoryPartyRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val discardItem = DiscardItem(inventoryRepository, PlayerTurnRule(SearchParty(partyRepository), SearchBattle(battleRepository)), eventBus)

    // It is the turn of the human player, who explores with the knight. The rat is no party member.
    @BeforeTest
    fun setUp() {
        battleRepository.create(battle(players = listOf("human", "cpu")))
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
    }

    @Test
    fun `should remove the item when the item is stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "helmet")))
        // When
        discardItem(battleUnitId = "knight-1", lootableItemId = "helmet")
        // Then
        inventoryRepository
            .searchByBattleUnitId("knight-1")
            ?.toDto()
            ?.storedItems
            ?.shouldContainExactly("health-potion")
        eventBus.shouldHavePublishedEvents(ItemDiscarded(battleUnitId = "knight-1", lootableItemId = "helmet"))
    }

    @Test
    fun `should fail when the item is not stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion")))
        // When
        val discarding = { discardItem(battleUnitId = "knight-1", lootableItemId = "helmet") }
        // Then
        shouldThrow<InventoryItemNotFound> { discarding() }
    }

    @Test
    fun `should fail when the only stored copy of the item is equipped`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // When
        val discarding = { discardItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<EquippedItemCanNotBeDiscarded> { discarding() }
    }

    @Test
    fun `should fail when it is not the turn of the party player`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human")))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("helmet")))
        // When
        val discarding = { discardItem(battleUnitId = "knight-1", lootableItemId = "helmet") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { discarding() }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("helmet")))
        // When
        val discarding = { discardItem(battleUnitId = "rat-1", lootableItemId = "helmet") }
        // Then
        shouldThrow<NotAPartyMember> { discarding() }
    }
}
