package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotEquippable
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class EquipItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val partyRepository = InMemoryPartyRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()
    private val equipItem =
        EquipItem(
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
        lootableItemRepository.create(
            equippableLootableItem(id = "hammer", equippable = equippable(effectIds = setOf("maximum-health-20", "maximum-mana-20"), equipmentZone = WEAPON)),
        )
        lootableItemRepository.create(equippableLootableItem(id = "axe", equippable = equippable(effectIds = setOf("maximum-health-10"), equipmentZone = WEAPON)))
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
    }

    @Test
    fun `should equip the item and request the application of its effects from the item to its battle unit when the item is stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer")))
        // When
        equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.equippedItems shouldBe mapOf(WEAPON to "hammer")
        eventBus.shouldHavePublishedEvents(
            ItemEquipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "hammer", effectId = "maximum-health-20"),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "hammer", effectId = "maximum-mana-20"),
        )
    }

    @Test
    fun `should apply the new item effects before removing the swapped item effects when the zone already holds an item`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer", "axe"), equippedItems = mapOf(WEAPON to "axe")))
        // When
        equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.equippedItems shouldBe mapOf(WEAPON to "hammer")
        eventBus.shouldHavePublishedEvents(
            ItemUnequipped(battleUnitId = "knight-1", lootableItemId = "axe", equipmentZone = WEAPON),
            ItemEquipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "hammer", effectId = "maximum-health-20"),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "hammer", effectId = "maximum-mana-20"),
            RequestRemoveMemberEquippedEffects(battleUnitId = "knight-1", lootableItemId = "axe"),
        )
    }

    @Test
    fun `should fail when the lootable item is not equippable`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion")))
        // When
        val equipping = { equipItem(battleUnitId = "knight-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<LootableItemNotEquippable> { equipping() }
    }

    @Test
    fun `should fail when the battle unit has not looted any item`() {
        // Given
        // When
        val equipping = { equipItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<InventoryItemNotFound> { equipping() }
    }

    @Test
    fun `should fail when it is not the turn of the party player`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human")))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer")))
        // When
        val equipping = { equipItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { equipping() }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("hammer")))
        // When
        val equipping = { equipItem(battleUnitId = "rat-1", lootableItemId = "hammer") }
        // Then
        shouldThrow<NotAPartyMember> { equipping() }
    }
}
