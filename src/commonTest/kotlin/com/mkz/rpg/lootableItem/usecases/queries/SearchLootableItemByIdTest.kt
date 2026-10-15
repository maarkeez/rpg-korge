package com.mkz.rpg.lootableItem.usecases.queries

import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchLootableItemByIdTest {
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val searchLootableItemById = SearchLootableItemById(lootableItemRepository = lootableItemRepository)

    @Test
    fun `should find lootable item when it exists`() {
        // Given
        val lootableItem = LootableItemMother.lootableItem()
        lootableItemRepository.create(lootableItem)
        // When
        val lootableItemFound = searchLootableItemById(id = lootableItem.toDto().id)
        // Then
        lootableItemFound shouldBe lootableItem.toDto()
    }

    @Test
    fun `should not find lootable item when it does not exist`() {
        // Given
        val lootableItemId = LootableItemMother.lootableItemId()
        // When
        val lootableItemFound = searchLootableItemById(id = lootableItemId)
        // Then
        lootableItemFound.shouldBeNull()
    }
}
