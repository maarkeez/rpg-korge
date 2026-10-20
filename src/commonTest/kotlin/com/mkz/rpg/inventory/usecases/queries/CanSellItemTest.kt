package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.TradeMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanSellItemTest {
    private val trade = TradeMother()
    private val canSellItem = CanSellItem(trade.inventoryRepository, trade.queries.searchLootableItemById, trade.tradeRule)

    @Test
    fun `should allow selling a stored copy that is not equipped while talking to a merchant`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion")))
        // Then
        canSellItem(battleUnitId = "viking-1", lootableItemId = "mana-potion").shouldBeTrue()
    }

    @Test
    fun `should not allow selling the only stored copy when it is equipped`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer"), equippedItems = mapOf(WEAPON to "hammer")))
        // Then
        canSellItem(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeFalse()
    }

    @Test
    fun `should not allow selling when the sell price takes the gold above 99,999`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("hammer"), gold = 99_975))
        // Then
        canSellItem(battleUnitId = "viking-1", lootableItemId = "hammer").shouldBeFalse()
    }

    @Test
    fun `should not allow selling when the party talks to no merchant`() {
        // Given
        trade.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion")))
        trade.talkTo("the-guy")
        // Then
        canSellItem(battleUnitId = "viking-1", lootableItemId = "mana-potion").shouldBeFalse()
    }
}
