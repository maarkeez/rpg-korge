package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchLootDropsSnapshotTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val searchLootDropsSnapshot = SearchLootDropsSnapshot(lootDropRepository)

    @Test
    fun `should return the items dropped by each unit`() {
        // Given
        val goblin = lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot"), droppedItems = mapOf(PositionDto(1, 1) to listOf("health-potion", "dagger")))
        val rat = lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))
        listOf(goblin, rat).forEach(lootDropRepository::create)
        // When
        val result = searchLootDropsSnapshot()
        // Then
        result.shouldContainExactlyInAnyOrder(goblin.toDto(), rat.toDto())
    }

    @Test
    fun `SearchLootDropsSnapshot should leave out the ownerless loot drop when it holds nothing`() {
        // Given
        lootDropRepository.create(LootDrop.ownerless())
        // When
        val result = searchLootDropsSnapshot()
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `SearchLootDropsSnapshot should hold the ownerless loot drop when an item lies in it`() {
        // Given
        val ownerless =
            LootDrop
                .ownerless()
                .drop(lootableItemId = "hammer", row = 1, column = 1)
                .pullEvents()
                .second
        lootDropRepository.create(ownerless)
        // When
        val result = searchLootDropsSnapshot()
        // Then
        result.shouldContainExactlyInAnyOrder(ownerless.toDto())
    }

    @Test
    fun `should return nothing when no unit has a loot drop`() {
        // When
        val result = searchLootDropsSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
