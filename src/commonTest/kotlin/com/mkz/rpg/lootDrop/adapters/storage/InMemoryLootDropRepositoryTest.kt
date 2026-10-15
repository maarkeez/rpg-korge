package com.mkz.rpg.lootDrop.adapters.storage

import com.mkz.rpg.lootDrop.domain.LootDropMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryLootDropRepositoryTest {
    private val lootDropRepository = InMemoryLootDropRepository()

    @Test
    fun `Create should store the loot drop when the loot drop is created`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        // When
        lootDropRepository.create(lootDrop)
        // Then
        val storedLootDrop = lootDropRepository.searchByUnitId(lootDrop.toDto().unitId)
        storedLootDrop shouldBe lootDrop
    }

    @Test
    fun `SearchByUnitId should return the loot drop when the loot drop of the unit is stored`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        lootDropRepository.create(lootDrop)
        // When
        val storedLootDrop = lootDropRepository.searchByUnitId(lootDrop.toDto().unitId)
        // Then
        storedLootDrop shouldBe lootDrop
    }

    @Test
    fun `SearchByUnitId should return null when the unit has no loot drop`() {
        // Given
        val unitId = LootDropMother.unitId()
        // When
        val storedLootDrop = lootDropRepository.searchByUnitId(unitId)
        // Then
        storedLootDrop.shouldBeNull()
    }
}
