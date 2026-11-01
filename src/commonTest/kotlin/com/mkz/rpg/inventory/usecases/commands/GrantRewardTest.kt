package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryEvent.RewardGranted
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class GrantRewardTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()
    private val grantReward = GrantReward(inventoryRepository, SearchLootableItemById(lootableItemRepository), eventBus)

    @BeforeTest
    fun setUp() {
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
    }

    @Test
    fun `GrantReward should store the reward in the inventory and announce it when the battle unit has space`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "hero", gold = 30))
        // When
        grantReward(battleUnitId = "hero", gold = 150, lootableItemIds = listOf("health-potion"), row = 1, column = 2)
        // Then
        val hero = inventoryRepository.searchByBattleUnitId("hero")!!.toDto()
        hero.gold shouldBe 180
        hero.storedItems shouldBe listOf("health-potion")
        eventBus.shouldHavePublishedEvents(RewardGranted(battleUnitId = "hero", gold = 150, lootableItemIds = listOf("health-potion")))
    }

    @Test
    fun `GrantReward should request dropping the items left when the bag is full`() {
        // Given
        inventoryRepository.create(fullInventory(battleUnitId = "hero"))
        // When
        grantReward(battleUnitId = "hero", gold = 0, lootableItemIds = listOf("health-potion", "health-potion"), row = 1, column = 2)
        // Then
        eventBus.shouldHavePublishedEvents(
            RewardGranted(battleUnitId = "hero", gold = 0, lootableItemIds = emptyList()),
            RequestDropItems(lootableItemIds = listOf("health-potion", "health-potion"), row = 1, column = 2),
        )
    }

    @Test
    fun `GrantReward should fail when a lootable item does not exist`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "hero"))
        // When
        val granting = { grantReward(battleUnitId = "hero", gold = 150, lootableItemIds = listOf("missing-lootable-item"), row = 1, column = 2) }
        // Then
        shouldThrow<InventoryLootableItemDoesNotExist> { granting() }
    }

    @Test
    fun `GrantReward should do nothing when the battle unit has no inventory`() {
        // When
        grantReward(battleUnitId = "bee-1", gold = 150, lootableItemIds = listOf("health-potion"), row = 1, column = 2)
        // Then
        inventoryRepository.searchByBattleUnitId("bee-1").shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
