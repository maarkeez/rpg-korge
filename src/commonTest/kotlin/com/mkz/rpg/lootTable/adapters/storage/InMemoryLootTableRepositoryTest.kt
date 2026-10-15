package com.mkz.rpg.lootTable.adapters.storage

import com.mkz.rpg.lootTable.domain.LootTableMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryLootTableRepositoryTest {
    private val lootTableRepository = InMemoryLootTableRepository()

    @Test
    fun `Create should store the loot table when the loot table is created`() {
        // Given
        val lootTable = LootTableMother.lootTable()
        // When
        lootTableRepository.create(lootTable)
        // Then
        val storedLootTable = lootTableRepository.searchById(lootTable.toDto().id)
        storedLootTable shouldBe lootTable
    }

    @Test
    fun `SearchById should return the loot table when the loot table is stored`() {
        // Given
        val lootTable = LootTableMother.lootTable()
        lootTableRepository.create(lootTable)
        // When
        val storedLootTable = lootTableRepository.searchById(lootTable.toDto().id)
        // Then
        storedLootTable shouldBe lootTable
    }

    @Test
    fun `SearchById should return null when the loot table is not stored`() {
        // Given
        val lootTableId = LootTableMother.lootTableId()
        // When
        val storedLootTable = lootTableRepository.searchById(lootTableId)
        // Then
        storedLootTable.shouldBeNull()
    }
}
