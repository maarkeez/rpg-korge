package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeSold
import com.mkz.rpg.inventory.domain.InventoryError.GoldAboveLimit
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SellItemTest {
    private val trade = TradeMother()
    private val eventBus = FakeEventBus()
    private val sellItem = SellItem(trade.inventoryRepository, trade.queries.searchSellPrice, trade.tradeRule, eventBus)

    // The viking carries 50 G, two hammers, the first one equipped, and a mana potion.
    @BeforeTest
    fun setUp() {
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer", "hammer", "mana-potion"), equippedItems = mapOf(WEAPON to "hammer"), gold = 50))
    }

    @Test
    fun `should remove the item and receive half its price, rounded down`() {
        // When
        sellItem(battleUnitId = "viking-1", lootableItemId = "hammer")
        // Then
        trade.gold("viking-1") shouldBe 75
        trade.storedItems("viking-1")!!.shouldContainExactly("hammer", "mana-potion")
        eventBus.shouldHavePublishedEvents(ItemSold(battleUnitId = "viking-1", lootableItemId = "hammer", price = 25))
    }

    @Test
    fun `should sell an item the merchant does not sell`() {
        // When
        sellItem(battleUnitId = "viking-1", lootableItemId = "mana-potion")
        // Then
        trade.gold("viking-1") shouldBe 55
    }

    @Test
    fun `should fail when the only stored copy is equipped`() {
        // Given
        sellItem(battleUnitId = "viking-1", lootableItemId = "hammer")
        // Then
        shouldThrow<EquippedItemCanNotBeSold> { sellItem(battleUnitId = "viking-1", lootableItemId = "hammer") }
    }

    @Test
    fun `should fail when the sell price takes the gold above 99,999`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion"), gold = 99_995))
        // Then
        shouldThrow<GoldAboveLimit> { sellItem(battleUnitId = "viking-1", lootableItemId = "mana-potion") }
    }

    @Test
    fun `should fail when the party does not talk to a merchant`() {
        // Given
        trade.talkTo("the-guy")
        // Then
        shouldThrow<NotTradingWithAMerchant> { sellItem(battleUnitId = "viking-1", lootableItemId = "mana-potion") }
    }
}
