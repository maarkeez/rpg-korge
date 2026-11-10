package com.mkz.rpg.lootDrop.adapters.save

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.commands.LoadLootDropsSnapshot
import com.mkz.rpg.lootDrop.usecases.queries.SearchLootDropsSnapshot
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LootDropSnapshottableTest {
    private val goblin = lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot"), droppedItems = mapOf(PositionDto(1, 1) to listOf("health-potion", "dagger")))
    private val rat = lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))

    private fun snapshottable(repository: LootDropRepository) = LootDropSnapshottable(SearchLootDropsSnapshot(repository), LoadLootDropsSnapshot(repository))

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryLootDropRepository().apply { listOf(goblin, rat).forEach(::create) }).snapshot()
        val loadedRepository =
            InMemoryLootDropRepository().apply {
                create(lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot")))
                create(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")))
            }
        // When
        snapshottable(loadedRepository).load(snapshot)
        // Then
        loadedRepository.searchAll().map { lootDrop -> lootDrop.toDto() }.shouldContainExactlyInAnyOrder(goblin.toDto(), rat.toDto())
    }

    @Test
    fun `Key should be lootDrop`() {
        // When
        val result = snapshottable(InMemoryLootDropRepository()).key
        // Then
        result shouldBe "lootDrop"
    }
}
