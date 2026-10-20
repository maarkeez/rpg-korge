package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeSold
import com.mkz.rpg.inventory.domain.InventoryError.GoldAboveLimit
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.ItemAlreadyEquipped
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.rpg.inventory.domain.InventoryError.NegativeGold
import com.mkz.rpg.inventory.domain.InventoryError.NoStorageSpaceLeft
import com.mkz.rpg.inventory.domain.InventoryError.NotEnoughGold
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.HEAD
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class InventoryTest {
    @Test
    fun `Create should create an empty inventory with 12 storage spaces and the starting gold when the battle unit is deployed`() {
        // Given
        val battleUnitId = "knight-1"
        // When
        val createdInventory = Inventory.create(battleUnitId, gold = 20)
        // Then
        createdInventory.toDto() shouldBe Inventory.Dto(battleUnitId = "knight-1", maximumStorageSpace = 12, storedItems = emptyList(), equippedItems = emptyMap(), gold = 20)
    }

    @Test
    fun `Create should create an inventory with up to 99,999 gold, and fail below 0 or above it`() {
        // Then
        Inventory.create("knight-1", gold = 99_999).toDto().gold shouldBe 99_999
        runCatching { Inventory.create("knight-1", gold = -1) }.exceptionOrNull().shouldBeTypeOf<NegativeGold>()
        runCatching { Inventory.create("knight-1", gold = 100_000) }.exceptionOrNull().shouldBeTypeOf<GoldAboveLimit>()
    }

    @Test
    fun `Buy should pay the price and store the item when it can afford it and has space left`() {
        // Given
        val inventory = inventory(battleUnitId = "viking-1", storedItems = listOf("health-potion"), gold = 100)
        // When
        val (events, updatedInventory) = inventory.buy("hammer", price = 50).pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("health-potion", "hammer")
        updatedInventory.toDto().gold shouldBe 50
        events.shouldContainExactly(ItemBought(battleUnitId = "viking-1", lootableItemId = "hammer", price = 50))
    }

    @Test
    fun `Buy should spend all the gold when the price is exactly the gold`() {
        // When
        val updatedInventory = inventory(gold = 10).buy("health-potion", price = 10)
        // Then
        updatedInventory.toDto().gold shouldBe 0
    }

    @Test
    fun `Buy should fail when it can not afford the price`() {
        // When
        val result = runCatching { inventory(gold = 20).buy("hammer", price = 50) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NotEnoughGold>()
    }

    @Test
    fun `Buy should fail when there is no space left`() {
        // When
        val result = runCatching { fullInventory(gold = 100).buy("hammer", price = 50) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NoStorageSpaceLeft>()
    }

    @Test
    fun `CanBuy should tell whether it can afford the price and has space left`() {
        // Then
        inventory(gold = 50).canBuy(price = 50).shouldBeTrue()
        inventory(gold = 49).canBuy(price = 50).shouldBeFalse()
        fullInventory(gold = 100).canBuy(price = 50).shouldBeFalse()
    }

    @Test
    fun `Sell should remove one of the stored items and receive its price when the item is stored`() {
        // Given
        val inventory = inventory(battleUnitId = "viking-1", storedItems = listOf("hammer", "hammer"), gold = 50)
        // When
        val (events, updatedInventory) = inventory.sell("hammer", price = 25).pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer")
        updatedInventory.toDto().gold shouldBe 75
        events.shouldContainExactly(ItemSold(battleUnitId = "viking-1", lootableItemId = "hammer", price = 25))
    }

    @Test
    fun `Sell should sell a copy that is not equipped when another copy is equipped`() {
        // Given
        val inventory = inventory(storedItems = listOf("hammer", "hammer"), equippedItems = mapOf(WEAPON to "hammer"))
        // When
        val updatedInventory = inventory.sell("hammer", price = 25)
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer")
        updatedInventory.toDto().equippedItems shouldBe mapOf(WEAPON to "hammer")
    }

    @Test
    fun `Sell should fail when the only stored copy is equipped`() {
        // When
        val result = runCatching { inventory(storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")).sell("hammer", price = 25) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EquippedItemCanNotBeSold>()
    }

    @Test
    fun `Sell should fail when the item is not stored`() {
        // When
        val result = runCatching { inventory().sell("hammer", price = 25) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InventoryItemNotFound>()
    }

    @Test
    fun `Sell should fail when the price takes the gold above 99,999`() {
        // When
        val result = runCatching { inventory(storedItems = listOf("hammer"), gold = 99_990).sell("hammer", price = 25) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<GoldAboveLimit>()
    }

    @Test
    fun `CanSell should tell whether a copy that is not equipped is stored, and the gold stays within 99,999`() {
        // Then
        inventory(storedItems = listOf("hammer"), gold = 99_974).canSell("hammer", price = 25).shouldBeTrue()
        inventory(storedItems = listOf("hammer"), gold = 99_975).canSell("hammer", price = 25).shouldBeFalse()
        inventory(storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")).canSell("hammer", price = 25).shouldBeFalse()
        inventory(storedItems = listOf("hammer", "hammer"), equippedItems = mapOf(WEAPON to "hammer")).canSell("hammer", price = 25).shouldBeTrue()
        inventory().canSell("hammer", price = 25).shouldBeFalse()
    }

    @Test
    fun `Store should store the item when there is space left`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("health-potion"))
        // When
        val (events, updatedInventory) = inventory.store("health-potion", row = 1, column = 2).pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("health-potion", "health-potion")
        events.shouldContainExactly(ItemStored(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2))
    }

    @Test
    fun `Store should store the item when it fills the last storage space`() {
        // Given
        val inventory = inventory(storedItems = List(11) { "health-potion" })
        // When
        val updatedInventory = inventory.store("helmet", row = 1, column = 2)
        // Then
        updatedInventory.toDto().storedItems.size shouldBe 12
        updatedInventory.toDto().storedItems.last() shouldBe "helmet"
    }

    @Test
    fun `Store should announce the inventory is full without storing the item when there is no space left`() {
        // Given
        val inventory = fullInventory(battleUnitId = "knight-1")
        // When
        val (events, updatedInventory) = inventory.store("health-potion", row = 1, column = 2).pullEvents()
        // Then
        updatedInventory.toDto() shouldBe inventory.toDto()
        events.shouldContainExactly(InventoryFull(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 2))
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
    fun `CanConsume should tell whether the item is stored`() {
        // Given
        val inventory = inventory(storedItems = listOf("teleport-essence"))
        // Then
        inventory.canConsume("teleport-essence").shouldBeTrue()
        inventory.canConsume("health-potion").shouldBeFalse()
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
    fun `Discard should remove a copy that is not equipped when another copy is equipped`() {
        // Given
        val inventory = inventory(storedItems = listOf("hammer", "hammer"), equippedItems = mapOf(WEAPON to "hammer"))
        // When
        val updatedInventory = inventory.discard("hammer")
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer")
        updatedInventory.toDto().equippedItems shouldBe mapOf(WEAPON to "hammer")
    }

    @Test
    fun `Discard should fail when the only stored copy is equipped`() {
        // Given
        val inventory = inventory(storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer"))
        // When
        val result = runCatching { inventory.discard("hammer") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EquippedItemCanNotBeDiscarded>()
    }

    @Test
    fun `Equip should equip the item in its zone while it keeps its storage space when the item is stored`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("hammer", "helmet"), equippedItems = mapOf(HEAD to "helmet"))
        // When
        val (events, updatedInventory) = inventory.equip("hammer", WEAPON).pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer", "helmet")
        updatedInventory.toDto().equippedItems shouldBe mapOf(HEAD to "helmet", WEAPON to "hammer")
        events.shouldContainExactly(ItemEquipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON))
    }

    @Test
    fun `Equip should unequip the item the zone held when the zone is already used`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("hammer", "axe"), equippedItems = mapOf(WEAPON to "axe"))
        // When
        val (events, updatedInventory) = inventory.equip("hammer", WEAPON).pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer", "axe")
        updatedInventory.toDto().equippedItems shouldBe mapOf(WEAPON to "hammer")
        events.shouldContainExactly(
            ItemUnequipped(battleUnitId = "knight-1", lootableItemId = "axe", equipmentZone = WEAPON),
            ItemEquipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON),
        )
    }

    @Test
    fun `Equip should fail when the item is not stored`() {
        // Given
        val inventory = inventory(storedItems = listOf("helmet"))
        // When
        val result = runCatching { inventory.equip("hammer", WEAPON) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InventoryItemNotFound>()
    }

    @Test
    fun `Equip should fail when the item is already equipped`() {
        // Given
        val inventory = inventory(storedItems = listOf("hammer", "hammer"), equippedItems = mapOf(WEAPON to "hammer"))
        // When
        val result = runCatching { inventory.equip("hammer", WEAPON) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ItemAlreadyEquipped>()
    }

    @Test
    fun `Unequip should unequip the item while it stays stored when the item is equipped`() {
        // Given
        val inventory = inventory(battleUnitId = "knight-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer"))
        // When
        val (events, updatedInventory) = inventory.unequip("hammer").pullEvents()
        // Then
        updatedInventory.toDto().storedItems.shouldContainExactly("hammer")
        updatedInventory.toDto().equippedItems shouldBe emptyMap()
        events.shouldContainExactly(ItemUnequipped(battleUnitId = "knight-1", lootableItemId = "hammer", equipmentZone = WEAPON))
    }

    @Test
    fun `Unequip should fail when the item is not equipped`() {
        // Given
        val inventory = inventory(storedItems = listOf("hammer"))
        // When
        val result = runCatching { inventory.unequip("hammer") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ItemNotEquipped>()
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val inventory = Inventory.create("knight-1").store("health-potion", row = 1, column = 2)
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
