package com.mkz.rpg.lootableItem.adapters.storage

import com.mkz.rpg.lootableItem.domain.LootableItemMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryLootableItemRepositoryTest {
    private val lootableItemRepository = InMemoryLootableItemRepository()

    @Test
    fun `Create should store the lootable item when the lootable item is created`() {
        // Given
        val lootableItem = LootableItemMother.lootableItem()
        // When
        lootableItemRepository.create(lootableItem)
        // Then
        val storedLootableItem = lootableItemRepository.searchById(lootableItem.toDto().id)
        storedLootableItem shouldBe lootableItem
    }

    @Test
    fun `SearchById should return the lootable item when the lootable item is stored`() {
        // Given
        val lootableItem = LootableItemMother.lootableItem()
        lootableItemRepository.create(lootableItem)
        // When
        val storedLootableItem = lootableItemRepository.searchById(lootableItem.toDto().id)
        // Then
        storedLootableItem shouldBe lootableItem
    }

    @Test
    fun `SearchById should return null when the lootable item is not stored`() {
        // Given
        val lootableItemId = LootableItemMother.lootableItemId()
        // When
        val storedLootableItem = lootableItemRepository.searchById(lootableItemId)
        // Then
        storedLootableItem.shouldBeNull()
    }
}
