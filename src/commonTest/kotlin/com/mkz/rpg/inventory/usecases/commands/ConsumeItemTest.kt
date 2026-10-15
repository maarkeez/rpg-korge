package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotConsumable
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConsumeItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()
    private val consumeItem =
        ConsumeItem(
            inventoryRepository,
            SearchLootableItemById(lootableItemRepository),
            PlayerTurnRule(SearchBattleUnitById(battleUnitRepository), SearchBattle(battleRepository)),
            eventBus,
        )

    // It is the turn of the human player, who owns the knight. The CPU player owns the rat.
    @BeforeTest
    fun setUp() {
        battleRepository.create(battle(players = listOf("human", "cpu")))
        battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "human").toDto()))
        battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto()))
        lootableItemRepository.create(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal", "cure"))))
        lootableItemRepository.create(equippableLootableItem(id = "helmet"))
    }

    @Test
    fun `should remove the consumable and request the application of its effects to its battle unit when the consumable is stored`() {
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
            RequestApplyEffect(EffectApplicationDto(source = ApplicationSourceDto.battleUnit("knight-1"), target = EffectTargetDto.Unit("knight-1"), effectId = "heal")),
            RequestApplyEffect(EffectApplicationDto(source = ApplicationSourceDto.battleUnit("knight-1"), target = EffectTargetDto.Unit("knight-1"), effectId = "cure")),
        )
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
    fun `should fail when it is not the turn of the battle unit player`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("health-potion")))
        // When
        val consumption = { consumeItem(battleUnitId = "rat-1", lootableItemId = "health-potion") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { consumption() }
    }
}
