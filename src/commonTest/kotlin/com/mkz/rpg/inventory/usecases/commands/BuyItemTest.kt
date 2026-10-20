package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.ItemNotSoldByTheMerchant
import com.mkz.rpg.inventory.domain.InventoryError.NoStorageSpaceLeft
import com.mkz.rpg.inventory.domain.InventoryError.NotEnoughGold
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class BuyItemTest {
    private val trade = TradeMother()
    private val eventBus = FakeEventBus()
    private val buyItem = BuyItem(trade.inventoryRepository, trade.queries.searchLootableItemById, trade.tradeRule, eventBus)

    // The knight has 20 G, and the viking 100 G.
    @BeforeTest
    fun setUp() {
        trade.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 20))
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("health-potion"), gold = 100))
    }

    @Test
    fun `should pay the price and store the item when the merchant sells it`() {
        // When
        buyItem(battleUnitId = "viking-1", lootableItemId = "hammer")
        // Then
        trade.gold("viking-1") shouldBe 50
        trade.storedItems("viking-1")!!.shouldContainExactly("health-potion", "hammer")
        eventBus.shouldHavePublishedEvents(ItemBought(battleUnitId = "viking-1", lootableItemId = "hammer", price = 50))
    }

    @Test
    fun `should fail when the battle unit can not afford the item`() {
        // Then
        shouldThrow<NotEnoughGold> { buyItem(battleUnitId = "knight-1", lootableItemId = "hammer") }
        trade.gold("knight-1") shouldBe 20
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the inventory has no space left`() {
        // Given
        trade.inventoryRepository.create(fullInventory(battleUnitId = "viking-1", gold = 100))
        // Then
        shouldThrow<NoStorageSpaceLeft> { buyItem(battleUnitId = "viking-1", lootableItemId = "health-potion") }
    }

    @Test
    fun `should fail when the merchant does not sell the item`() {
        // Then
        shouldThrow<ItemNotSoldByTheMerchant> { buyItem(battleUnitId = "viking-1", lootableItemId = "mana-potion") }
    }

    @Test
    fun `should fail when the party talks to an npc that is no merchant`() {
        // Given
        trade.talkTo("the-guy")
        // Then
        shouldThrow<NotTradingWithAMerchant> { buyItem(battleUnitId = "viking-1", lootableItemId = "hammer") }
    }

    @Test
    fun `should fail when the party talks to no one`() {
        // Given
        trade.explore()
        // Then
        shouldThrow<NotTradingWithAMerchant> { buyItem(battleUnitId = "viking-1", lootableItemId = "hammer") }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Then
        shouldThrow<NotTradingWithAMerchant> { buyItem(battleUnitId = "rat-1", lootableItemId = "hammer") }
        trade.gold("rat-1") shouldBe 100
    }
}
