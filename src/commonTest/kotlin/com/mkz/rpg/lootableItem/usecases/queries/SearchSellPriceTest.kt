package com.mkz.rpg.lootableItem.usecases.queries

import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchSellPriceTest {
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val searchSellPrice = SearchSellPrice(lootableItemRepository = lootableItemRepository)

    @Test
    fun `should return half the price of the lootable item when it exists`() {
        // Given
        lootableItemRepository.create(LootableItemMother.lootableItem(id = "hammer", price = 50))
        // When
        val sellPrice = searchSellPrice(id = "hammer")
        // Then
        sellPrice shouldBe 25
    }

    @Test
    fun `should return null when the lootable item does not exist`() {
        // Given
        val lootableItemId = LootableItemMother.lootableItemId()
        // When
        val sellPrice = searchSellPrice(id = lootableItemId)
        // Then
        sellPrice.shouldBeNull()
    }
}
