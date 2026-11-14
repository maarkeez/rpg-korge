package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotStored
import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.tessavar.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.usecases.services.PlayerTurnRule
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
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
        shouldThrow<ItemNotStored> { discarding() }
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
    fun `should fail when it is not the player turn of the human player`() {
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
