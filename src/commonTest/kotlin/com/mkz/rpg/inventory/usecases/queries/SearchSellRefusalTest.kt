package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchSellRefusalTest {
    private val trade = TradeMother()
    private val searchSellRefusal = SearchSellRefusal(trade.inventoryRepository, trade.queries.searchSellPrice, trade.tradeRule)

    @Test
    fun `should not refuse selling a stored copy that is not equipped while talking to a merchant`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion")))
        // Then
        searchSellRefusal(battleUnitId = "viking-1", lootableItemId = "mana-potion").shouldBeNull()
    }

    @Test
    fun `should refuse selling an item that is not stored`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1"))
        // Then
        searchSellRefusal(battleUnitId = "viking-1", lootableItemId = "mana-potion") shouldBe TradeRefusalDto.NOT_STORED
    }

    @Test
    fun `should refuse selling the only stored copy when it is equipped`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // Then
        searchSellRefusal(battleUnitId = "viking-1", lootableItemId = "hammer") shouldBe TradeRefusalDto.EQUIPPED
    }

    @Test
    fun `should refuse selling when the sell price takes the gold above 99,999`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer"), gold = 99_975))
        // Then
        searchSellRefusal(battleUnitId = "viking-1", lootableItemId = "hammer") shouldBe TradeRefusalDto.GOLD_FULL
    }

    @Test
    fun `should refuse selling when the party talks to no merchant`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion")))
        trade.talkTo("the-guy")
        // Then
        searchSellRefusal(battleUnitId = "viking-1", lootableItemId = "mana-potion") shouldBe TradeRefusalDto.NOT_TRADING
    }
}
