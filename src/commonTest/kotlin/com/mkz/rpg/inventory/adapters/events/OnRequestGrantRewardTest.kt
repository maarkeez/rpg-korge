package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.commands.GrantReward
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestGrantRewardTest {
    private val eventBus = InMemoryEventBus()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val onRequestGrantReward =
        OnRequestGrantReward(GrantReward(inventoryRepository, SearchLootableItemById(InMemoryLootableItemRepository()), eventBus), eventBus)

    @Test
    fun `should grant the reward when it is requested`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "hero", gold = 30))
        eventBus.publish(RequestGrantReward(battleUnitId = "hero", gold = 150, lootableItemIds = emptyList(), row = 1, column = 2))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("hero")?.toDto()?.gold shouldBe 180
    }
}
