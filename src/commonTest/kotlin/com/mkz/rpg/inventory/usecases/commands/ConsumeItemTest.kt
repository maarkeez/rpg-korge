package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.learnAbilityEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.AbilityCanNotBeLearned
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotConsumable
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.ConsumptionRule
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.queries.CanMemberLearnAbility
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConsumeItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val partyRepository = InMemoryPartyRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val consumeItem =
        ConsumeItem(
            inventoryRepository,
            SearchLootableItemById(lootableItemRepository),
            PlayerTurnRule(SearchParty(partyRepository), SearchBattle(battleRepository)),
            ConsumptionRule(SearchEffectById(effectRepository), CanMemberLearnAbility(partyRepository, CanLearnAbility(battleUnitRepository))),
            eventBus,
        )

    // It is the turn of the human player, who explores with the knight, which knows teleport. The rat is no party member.
    // The teleport essence teaches teleport.
    @BeforeTest
    fun setUp() {
        battleRepository.create(battle(players = listOf("human", "cpu")))
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
        battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("teleport")).toDto()))
        lootableItemRepository.create(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal", "cure"))))
        lootableItemRepository.create(equippableLootableItem(id = "helmet"))
        effectRepository.create(learnAbilityEffect(id = "learn-teleport", abilityId = "teleport"))
        lootableItemRepository.create(consumableLootableItem(id = "teleport-essence", consumable = consumable(effectIds = setOf("learn-teleport"))))
    }

    @Test
    fun `should remove the consumable and request the application of its effects from the item to its battle unit when the consumable is stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "helmet")))
        // When
        consumeItem(battleUnitId = "knight-1", lootableItemId = "health-potion")
        // Then
        inventoryRepository
            .searchByBattleUnitId("knight-1")
            ?.toDto()
            ?.storedItems
            ?.shouldContainExactly("helmet")
        eventBus.shouldHavePublishedEvents(
            ItemConsumed(battleUnitId = "knight-1", lootableItemId = "health-potion"),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "health-potion", effectId = "heal"),
            RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "health-potion", effectId = "cure"),
        )
    }

    @Test
    fun `should fail keeping the consumable stored when the battle unit can not learn the ability it teaches`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("teleport-essence")))
        // When
        val consumption = { consumeItem(battleUnitId = "knight-1", lootableItemId = "teleport-essence") }
        // Then
        shouldThrow<AbilityCanNotBeLearned> { consumption() }
        inventoryRepository
            .searchByBattleUnitId("knight-1")
            ?.toDto()
            ?.storedItems
            ?.shouldContainExactly("teleport-essence")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the lootable item is not consumable`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("helmet")))
        // When
        val consumption = { consumeItem(battleUnitId = "knight-1", lootableItemId = "helmet") }
        // Then
        shouldThrow<LootableItemNotConsumable> { consumption() }
    }

    @Test
    fun `should fail when the consumable is not stored`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("helmet")))
        // When
        val consumption = { consumeItem(battleUnitId = "knight-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<InventoryItemNotFound> { consumption() }
    }

    @Test
    fun `should fail when the battle unit has not looted any item`() {
        // Given
        // When
        val consumption = { consumeItem(battleUnitId = "knight-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<InventoryItemNotFound> { consumption() }
    }

    @Test
    fun `should fail when it is not the turn of the party player`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human")))
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion")))
        // When
        val consumption = { consumeItem(battleUnitId = "knight-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { consumption() }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("health-potion")))
        // When
        val consumption = { consumeItem(battleUnitId = "rat-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<NotAPartyMember> { consumption() }
    }

    @Test
    fun `should consume the item out of the battles when the battle has finished`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human")).finishWithoutWinner())
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion")))
        // When
        consumeItem(battleUnitId = "knight-1", lootableItemId = "health-potion")
        // Then
        inventoryRepository
            .searchByBattleUnitId("knight-1")!!
            .toDto()
            .storedItems
            .shouldBeEmpty()
    }
}
