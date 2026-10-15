package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.lootDrop.domain.LootDropError.DuplicatedLootDropLootTable
import com.mkz.rpg.lootDrop.domain.LootDropError.EmptyLootDropLootTables
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.shared.domain.FixedRandom
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class LootDropTest {
    // The health potion drops on rolls 0 to 9, and nothing drops on rolls 10 to 14.
    private val tierOne = lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))).toDto()

    @Test
    fun `Create should create loot drop when the dto is valid`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop(lootTableIds = listOf("tier-1", "tier-2")).toDto()
        // When
        val createdLootDrop = LootDrop.create(lootDropDto)
        // Then
        createdLootDrop.toDto() shouldBe lootDropDto
    }

    @Test
    fun `Create should fail when there are no loot tables`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop().toDto().copy(lootTableIds = emptyList())
        // When
        val result = runCatching { LootDrop.create(lootDropDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EmptyLootDropLootTables>()
    }

    @Test
    fun `Create should fail when a loot table is repeated`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop().toDto().copy(lootTableIds = listOf("tier-1", "tier-1"))
        // When
        val result = runCatching { LootDrop.create(lootDropDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<DuplicatedLootDropLootTable>()
    }

    @Test
    fun `Drop should drop the lootable item when the roll falls within its weight`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1"))
        // When
        val droppedLootableItemId = lootDrop.drop(lootTables = listOf(tierOne), random = FixedRandom(roll = 9))
        // Then
        droppedLootableItemId shouldBe "health-potion"
    }

    @Test
    fun `Drop should drop nothing when the roll falls within the nothing weight`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1"))
        // When
        val droppedLootableItemId = lootDrop.drop(lootTables = listOf(tierOne), random = FixedRandom(roll = 10))
        // Then
        droppedLootableItemId.shouldBeNull()
    }

    @Test
    fun `Drop should drop a lootable item of the next loot table when the roll falls after the weights of the previous loot table`() {
        // Given
        val tierTwo = lootTable(id = "tier-2", entries = listOf(lootableItemEntry(lootableItemId = "mana-potion", weight = 5))).toDto()
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1", "tier-2"))
        // When
        val droppedLootableItemId = lootDrop.drop(lootTables = listOf(tierOne, tierTwo), random = FixedRandom(roll = 15))
        // Then
        droppedLootableItemId shouldBe "mana-potion"
    }

    @Test
    fun `PullEvents should pull the created event when the loot drop has pending events`() {
        // Given
        val createdLootDrop = LootDrop.create(LootDropMother.lootDrop().toDto())
        // When
        val (events, _) = createdLootDrop.pullEvents()
        // Then
        events.shouldContainExactly(LootDropCreated(unitId = createdLootDrop.toDto().unitId))
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val createdLootDrop = LootDrop.create(LootDropMother.lootDrop().toDto())
        // When
        val (_, clearedLootDrop) = createdLootDrop.pullEvents()
        // Then
        clearedLootDrop.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        // When
        val (events, _) = lootDrop.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
