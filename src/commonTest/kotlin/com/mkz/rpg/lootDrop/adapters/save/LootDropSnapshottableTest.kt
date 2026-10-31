package com.mkz.rpg.lootDrop.adapters.save

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.commands.RestoreLootDrops
import com.mkz.rpg.lootDrop.usecases.queries.SearchLootDropsSnapshot
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LootDropSnapshottableTest {
    // The goblin dropped a health potion and a dagger at (1,1), the rat nothing.
    private val goblin = lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot"), droppedItems = mapOf(PositionDto(1, 1) to listOf("health-potion", "dagger")))
    private val rat = lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))

    private fun snapshottable(repository: LootDropRepository) = LootDropSnapshottable(SearchLootDropsSnapshot(repository), RestoreLootDrops(repository))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given: as read from the resources, nothing dropped yet.
        val snapshot = snapshottable(InMemoryLootDropRepository().apply { listOf(goblin, rat).forEach(::create) }).snapshot()
        val restoredRepository =
            InMemoryLootDropRepository().apply {
                create(lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot")))
                create(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")))
            }
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.searchAll().map { lootDrop -> lootDrop.toDto() }.shouldContainExactlyInAnyOrder(goblin.toDto(), rat.toDto())
    }

    @Test
    fun `Key should be lootDrop`() {
        // When
        val result = snapshottable(InMemoryLootDropRepository()).key
        // Then
        result shouldBe "lootDrop"
    }
}
