package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchBuyRefusalTest {
    private val trade = TradeMother()
    private val searchBuyRefusal = SearchBuyRefusal(trade.inventoryRepository, trade.queries.searchLootableItemById, trade.tradeRule)

    // The knight has 20 G, and the viking 100 G.
    @BeforeTest
    fun setUp() {
        trade.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 20))
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 100))
    }

    @Test
    fun `should not refuse buying when the merchant sells the item, and the battle unit can afford it and has space left`() {
        // Then
        searchBuyRefusal(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeNull()
        searchBuyRefusal(battleUnitId = "knight-1", lootableItemId = "health-potion").shouldBeNull()
    }

    @Test
    fun `should refuse buying when the battle unit can not afford the item`() {
        // Then
        searchBuyRefusal(battleUnitId = "knight-1", lootableItemId = "hammer") shouldBe TradeRefusalDto.NOT_ENOUGH_GOLD
    }

    @Test
    fun `should refuse buying when the inventory has no space left`() {
        // Given
        trade.inventoryRepository.create(fullInventory(battleUnitId = "viking-1", gold = 100))
        // Then
        searchBuyRefusal(battleUnitId = "viking-1", lootableItemId = "hammer") shouldBe TradeRefusalDto.BAG_FULL
    }

    @Test
    fun `should refuse buying when the merchant does not sell the item, or the party talks to no merchant`() {
        // Then
        searchBuyRefusal(battleUnitId = "viking-1", lootableItemId = "mana-potion") shouldBe TradeRefusalDto.NOT_SOLD_BY_THE_MERCHANT
        // When
        trade.explore()
        // Then
        searchBuyRefusal(battleUnitId = "viking-1", lootableItemId = "hammer") shouldBe TradeRefusalDto.NOT_TRADING
    }
}
