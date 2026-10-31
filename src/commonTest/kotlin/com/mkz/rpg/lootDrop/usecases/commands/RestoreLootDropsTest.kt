package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class RestoreLootDropsTest {
    // As read from the resources: the goblin dropped nothing yet, the rat some cheese since.
    private val lootDropRepository =
        InMemoryLootDropRepository().apply {
            create(lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot")))
            create(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"), droppedItems = mapOf(PositionDto(0, 0) to listOf("cheese"))))
        }
    private val restoreLootDrops = RestoreLootDrops(lootDropRepository)

    @Test
    fun `should rebuild the same state when it restores the snapshot it took`() {
        // Given: the goblin dropped a health potion and a dagger at (1,1), the rat nothing.
        val goblin = lootDrop(unitId = "goblin", lootTableIds = listOf("goblin-loot"), droppedItems = mapOf(PositionDto(1, 1) to listOf("health-potion", "dagger")))
        val rat = lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot"))
        // When
        restoreLootDrops(listOf(goblin, rat).associate { lootDrop -> lootDrop.toDto().unitId to lootDrop.toDto().droppedItems })
        // Then
        lootDropRepository.searchAll().map { lootDrop -> lootDrop.toDto() }.shouldContainExactlyInAnyOrder(goblin.toDto(), rat.toDto())
    }

    @Test
    fun `should fail when a unit no longer drops loot`() {
        // When
        val result = runCatching { restoreLootDrops(mapOf("orc" to mapOf(PositionDto(1, 1) to listOf("dagger")))) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "orc"
    }
}
