package com.mkz.rpg.lootTable.usecases.queries

import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchLootTableByIdTest {
    private val lootTableRepository = InMemoryLootTableRepository()
    private val searchLootTableById = SearchLootTableById(lootTableRepository = lootTableRepository)

    @Test
    fun `should find loot table when it exists`() {
        // Given
        val lootTable = LootTableMother.lootTable()
        lootTableRepository.create(lootTable)
        // When
        val lootTableFound = searchLootTableById(id = lootTable.toDto().id)
        // Then
        lootTableFound shouldBe lootTable.toDto()
    }

    @Test
    fun `should not find loot table when it does not exist`() {
        // Given
        val lootTableId = LootTableMother.lootTableId()
        // When
        val lootTableFound = searchLootTableById(id = lootTableId)
        // Then
        lootTableFound.shouldBeNull()
    }
}
