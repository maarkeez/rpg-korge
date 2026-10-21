package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.LootFound
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnLootFoundTest {
    private val eventBus = InMemoryEventBus()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val playerRepository = InMemoryPlayerRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val onLootFound =
        OnLootFound(
            storeLootedItem =
                StoreLootedItem(
                    inventoryRepository,
                    SearchBattleUnitById(battleUnitRepository),
                    SearchLootableItemById(lootableItemRepository),
                    eventBus,
                ),
            eventBus = eventBus,
        )

    @Test
    fun `should store every item found in the inventory when a battle unit finds loot`() {
        // Given
        val human = player(id = "human", type = HUMAN)
        playerRepository.create(human)
        battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
        lootableItemRepository.create(consumableLootableItem(id = "antidote"))
        inventoryRepository.create(inventory(battleUnitId = "knight-1"))
        eventBus.publish(LootFound(occupantId = "knight-1", lootableItemIds = listOf("health-potion", "antidote"), row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.storedItems shouldBe listOf("health-potion", "antidote")
    }
}
