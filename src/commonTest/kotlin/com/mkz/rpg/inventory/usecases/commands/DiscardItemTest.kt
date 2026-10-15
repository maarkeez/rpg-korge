package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class DiscardItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val discardItem = DiscardItem(inventoryRepository, PlayerTurnRule(SearchBattleUnitById(battleUnitRepository), SearchBattle(battleRepository)), eventBus)

    // It is the turn of the human player, who owns the knight. The CPU player owns the rat.
    @BeforeTest
    fun setUp() {
        battleRepository.create(battle(players = listOf("human", "cpu")))
        battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "human").toDto()))
        battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto()))
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
    fun `should fail when it is not the turn of the battle unit player`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "rat-1", storedItems = listOf("helmet")))
        // When
        val discarding = { discardItem(battleUnitId = "rat-1", lootableItemId = "helmet") }
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { discarding() }
    }

    @Test
    fun `should fail when the battle unit does not exist`() {
        // Given
        // When
        val discarding = { discardItem(battleUnitId = "missing-battle-unit", lootableItemId = "helmet") }
        // Then
        shouldThrow<InventoryBattleUnitDoesNotExist> { discarding() }
    }
}
