package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class InventoryTest {
    @Test
    fun `Create should create an empty inventory with 12 storage spaces when the battle unit loots its first item`() {
        // Given
        val battleUnitId = "knight-1"
        // When
        val createdInventory = Inventory.create(battleUnitId)
        // Then
        createdInventory.toDto() shouldBe Inventory.Dto(battleUnitId = "knight-1", maximumStorageSpace = 12, storedItems = emptyList())
    }

    @Test
    fun `Store should store the item when there is space left`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion"))
        // When
        val (events, updatedInventory) = inventory.store("health-potion").pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("health-potion", "health-potion")
        events.shouldContainExactly(ItemStored(battleUnitId = "knight-1", lootableItemId = "health-potion"))
    }

    @Test
    fun `Store should store the item when it fills the last storage space`() {
        // Given
        val inventory = inventory(storedItems = List(11) { "health-potion" })
        // When
        val updatedInventory = inventory.store("helmet")
        // Then
        updatedInventory.toDto().storedItems.size shouldBe 12
        updatedInventory.toDto().storedItems.last() shouldBe "helmet"
    }

    @Test
    fun `Store should announce the inventory is full without storing the item when there is no space left`() {
        // Given
        val inventory = fullInventory(battleUnitId = "knight-1")
        // When
        val (events, updatedInventory) = inventory.store("health-potion").pullEvents()
        // Then
        updatedInventory.toDto() shouldBe inventory.toDto()
        events.shouldContainExactly(InventoryFull(battleUnitId = "knight-1", lootableItemId = "health-potion"))
    }

    @Test
    fun `CanStore should allow storing when there is space left`() {
        // Given
        val inventory = inventory(storedItems = List(11) { "health-potion" })
        // When
        val canStore = inventory.canStore()
        // Then
        canStore.shouldBeTrue()
    }

    @Test
    fun `CanStore should not allow storing when there is no space left`() {
        // Given
        val inventory = fullInventory()
        // When
        val canStore = inventory.canStore()
        // Then
        canStore.shouldBeFalse()
    }

    @Test
    fun `Consume should remove one of the stored items when the item is stored`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion", "helmet", "health-potion"))
        // When
        val (events, updatedInventory) = inventory.consume("health-potion").pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("helmet", "health-potion")
        events.shouldContainExactly(ItemConsumed(battleUnitId = "knight-1", lootableItemId = "health-potion"))
    }

    @Test
    fun `Consume should fail when the item is not stored`() {
        // Given
        val inventory = inventory(storedItems = listOf("helmet"))
        // When
        val result = runCatching { inventory.consume("health-potion") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InventoryItemNotFound>()
    }

    @Test
    fun `Discard should remove one of the stored items and free its space when the item is stored`() {
        // Given
        val inventory = fullInventory(battleUnitId = "knight-1")
        val discardedItem = inventory.toDto().storedItems.first()
        // When
        val (events, updatedInventory) = inventory.discard(discardedItem).pullEvents()
        // Then
        updatedInventory.toDto().storedItems shouldBe inventory.toDto().storedItems.drop(1)
        updatedInventory.canStore().shouldBeTrue()
        events.shouldContainExactly(ItemDiscarded(battleUnitId = "knight-1", lootableItemId = discardedItem))
    }

    @Test
    fun `Discard should fail when the item is not stored`() {
        // Given
        val inventory = inventory(storedItems = listOf("helmet"))
        // When
        val result = runCatching { inventory.discard("health-potion") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InventoryItemNotFound>()
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val inventory = Inventory.create("knight-1").store("health-potion")
        // When
        val (_, clearedInventory) = inventory.pullEvents()
        // Then
        clearedInventory.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val inventory = Inventory.create("knight-1")
        // When
        val (events, _) = inventory.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
