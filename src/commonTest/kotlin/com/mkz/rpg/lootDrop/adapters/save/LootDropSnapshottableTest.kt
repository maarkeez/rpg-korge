package com.mkz.rpg.lootDrop.adapters.save

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class LootDropSnapshottableTest {
    // The goblin dropped a health potion and a dagger at (1,1), the rat nothing.
    private val goblin = lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot"), droppedItems = mapOf(PositionDto(1, 1) to listOf("health-potion", "dagger")))
    private val rat = lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))

    // As read from the resources: nothing dropped yet.
    private fun resourcesRepository() =
        InMemoryLootDropRepository().apply {
            create(lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot")))
            create(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"), droppedItems = mapOf(PositionDto(0, 0) to listOf("cheese"))))
        }

    private fun snapshot(): String = LootDropSnapshottable(InMemoryLootDropRepository().apply { listOf(goblin, rat).forEach(::create) }).snapshot()

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val restoredRepository = resourcesRepository()
        // When
        LootDropSnapshottable(restoredRepository).restore(snapshot())
        // Then
        restoredRepository.searchAll().map { lootDrop -> lootDrop.toDto() }.shouldContainExactlyInAnyOrder(goblin.toDto(), rat.toDto())
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val restoredRepository = resourcesRepository()
        // When
        LootDropSnapshottable(restoredRepository).restore(snapshot())
        // Then
        restoredRepository.searchAll().flatMap { lootDrop -> lootDrop.pullEvents().first }.shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when a unit no longer drops loot`() {
        // Given
        val restoredRepository = InMemoryLootDropRepository().apply { create(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))) }
        // When
        val result = runCatching { LootDropSnapshottable(restoredRepository).restore(snapshot()) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "goblin"
    }
}
