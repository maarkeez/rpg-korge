package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

class CanBuyItemTest {
    private val trade = TradeMother()
    private val canBuyItem = CanBuyItem(trade.inventoryRepository, trade.queries.searchLootableItemById, trade.tradeRule)

    // The knight has 20 G, and the viking 100 G.
    @BeforeTest
    fun setUp() {
        trade.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 20))
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 100))
    }

    @Test
    fun `should allow buying when the merchant sells the item, and the battle unit can afford it and has space left`() {
        // Then
        canBuyItem(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeTrue()
        canBuyItem(battleUnitId = "knight-1", lootableItemId = "health-potion").shouldBeTrue()
    }

    @Test
    fun `should not allow buying when the battle unit can not afford the item`() {
        // Then
        canBuyItem(battleUnitId = "knight-1", lootableItemId = "hammer").shouldBeFalse()
    }

    @Test
    fun `should not allow buying when the inventory has no space left`() {
        // Given
        trade.inventoryRepository.create(fullInventory(battleUnitId = "viking-1", gold = 100))
        // Then
        canBuyItem(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeFalse()
    }

    @Test
    fun `should not allow buying when the merchant does not sell the item, or the party talks to no merchant`() {
        // Then
        canBuyItem(battleUnitId = "viking-1", lootableItemId = "mana-potion").shouldBeFalse()
        // When
        trade.explore()
        // Then
        canBuyItem(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeFalse()
    }
}
