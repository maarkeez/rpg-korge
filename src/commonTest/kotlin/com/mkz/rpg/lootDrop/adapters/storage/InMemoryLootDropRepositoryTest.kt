package com.mkz.rpg.lootDrop.adapters.storage

import com.mkz.rpg.lootDrop.domain.LootDropMother
import io.kotest.matchers.collections.shouldContainExactly
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

    @Test
    fun `Update should replace the loot drop of the unit when the loot drop is stored`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        lootDropRepository.create(lootDrop)
        val updatedLootDrop = lootDrop.place(lootableItemId = "health-potion", row = 0, column = 1).pullEvents().second
        // When
        lootDropRepository.update(updatedLootDrop)
        // Then
        lootDropRepository.searchByUnitId(lootDrop.toDto().unitId) shouldBe updatedLootDrop
    }

    @Test
    fun `SearchAll should return every loot drop in creation order when loot drops are stored`() {
        // Given
        val firstLootDrop = LootDropMother.lootDrop()
        val secondLootDrop = LootDropMother.lootDrop()
        lootDropRepository.create(firstLootDrop)
        lootDropRepository.create(secondLootDrop)
        // When
        val lootDrops = lootDropRepository.searchAll()
        // Then
        lootDrops.shouldContainExactly(firstLootDrop, secondLootDrop)
    }
}
