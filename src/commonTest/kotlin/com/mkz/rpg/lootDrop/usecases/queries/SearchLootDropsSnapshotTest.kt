package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
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
    fun `should return nothing when no unit drops loot`() {
        // When
        val result = searchLootDropsSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
