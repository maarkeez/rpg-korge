package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestPickUpDroppedItem
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class StoreLootedItemTest {
    private val inventoryRepository = InMemoryInventoryRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()
    private val storeLootedItem =
        StoreLootedItem(inventoryRepository, SearchLootableItemById(lootableItemRepository), eventBus)

    // Only the knight has an inventory, as a party member. The rat and the summoned bee have none.
    @BeforeTest
    fun setUp() {
        lootableItemRepository.create(consumableLootableItem(id = "health-potion"))
        inventoryRepository.create(inventory(battleUnitId = "knight-1"))
    }

    @Test
    fun `should store the item in the inventory when the battle unit loots its first item`() {
        // Given
        // When
        storeLootedItem(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2)
        // Then
        inventoryRepository
            .searchByBattleUnitId("knight-1")
            ?.toDto()
            ?.storedItems
            .shouldBe(listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(
            ItemStored(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2),
            RequestPickUpDroppedItem(lootableItemId = "health-potion", row = 1, column = 2),
        )
    }

    @Test
    fun `should store the item next to the stored items when the human battle unit has already looted items`() {
        // Given
        inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion")))
        // When
        storeLootedItem(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2)
        // Then
        inventoryRepository
            .searchByBattleUnitId("knight-1")
            ?.toDto()
            ?.storedItems
            ?.shouldContainExactly("health-potion", "health-potion")
        eventBus.shouldHavePublishedEvents(
            ItemStored(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2),
            RequestPickUpDroppedItem(lootableItemId = "health-potion", row = 1, column = 2),
        )
    }

    @Test
    fun `should announce the inventory is full without storing the item when the inventory is full`() {
        // Given
        val inventory = fullInventory(battleUnitId = "knight-1")
        inventoryRepository.create(inventory)
        // When
        storeLootedItem(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2)
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto() shouldBe inventory.toDto()
        eventBus.shouldHavePublishedEvents(InventoryFull(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2))
    }

    @Test
    fun `should leave the item on its tile when the battle unit has no inventory, as it belongs to the CPU player`() {
        // Given
        // When
        storeLootedItem(battleUnitId = "rat-1", lootableItemId = "health-potion", row = 1, column = 2)
        // Then
        inventoryRepository.searchByBattleUnitId("rat-1").shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should leave the item on its tile when the battle unit has no inventory, as it is summoned`() {
        // Given
        // When
        storeLootedItem(battleUnitId = "bee-1", lootableItemId = "health-potion", row = 1, column = 2)
        // Then
        inventoryRepository.searchByBattleUnitId("bee-1").shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the lootable item does not exist`() {
        // Given
        // When
        val storing = { storeLootedItem(battleUnitId = "knight-1", lootableItemId = "missing-lootable-item", row = 1, column = 2) }
        // Then
        shouldThrow<InventoryLootableItemDoesNotExist> { storing() }
    }
}
