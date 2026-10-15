package com.mkz.rpg.lootTable.domain

import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto.TypeDto.LOOTABLE_ITEM
import com.mkz.rpg.lootTable.domain.LootTableError.DuplicatedLootTableEntry
import com.mkz.rpg.lootTable.domain.LootTableError.EmptyLootTableEntries
import com.mkz.rpg.lootTable.domain.LootTableError.InvalidLootTableEntryType
import com.mkz.rpg.lootTable.domain.LootTableError.NonPositiveLootTableWeight
import com.mkz.rpg.lootTable.domain.LootTableEvent.LootTableCreated
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class LootTableTest {
    @Test
    fun `Create should create loot table when the dto is valid`() {
        // Given
        val lootTableDto = LootTableMother.lootTable(entries = listOf(lootableItemEntry(weight = 10), nothingEntry(weight = 5))).toDto()
        // When
        val createdLootTable = LootTable.create(lootTableDto)
        // Then
        createdLootTable.toDto() shouldBe lootTableDto
    }

    @Test
    fun `Create should create loot table when it only drops nothing`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = listOf(nothingEntry()))
        // When
        val createdLootTable = LootTable.create(lootTableDto)
        // Then
        createdLootTable.toDto() shouldBe lootTableDto
    }

    @Test
    fun `Create should fail when there are no entries`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = emptyList())
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EmptyLootTableEntries>()
    }

    @Test
    fun `Create should fail when a weight is zero`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = listOf(lootableItemEntry(weight = 0)))
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NonPositiveLootTableWeight>()
    }

    @Test
    fun `Create should fail when a weight is negative`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = listOf(nothingEntry(weight = -1)))
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NonPositiveLootTableWeight>()
    }

    @Test
    fun `Create should fail when a lootable item is repeated`() {
        // Given
        val lootTableDto =
            LootTableMother
                .lootTable()
                .toDto()
                .copy(entries = listOf(lootableItemEntry(lootableItemId = "health-potion"), lootableItemEntry(lootableItemId = "health-potion")))
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<DuplicatedLootTableEntry>()
    }

    @Test
    fun `Create should fail when nothing is repeated`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = listOf(nothingEntry(), nothingEntry()))
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<DuplicatedLootTableEntry>()
    }

    @Test
    fun `Create should fail when a lootable item entry has no lootable item`() {
        // Given
        val lootTableDto = LootTableMother.lootTable().toDto().copy(entries = listOf(EntryDto(type = LOOTABLE_ITEM, weight = 10, lootableItemId = null)))
        // When
        val result = runCatching { LootTable.create(lootTableDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InvalidLootTableEntryType>()
    }

    @Test
    fun `PullEvents should pull the created event when the loot table has pending events`() {
        // Given
        val createdLootTable = LootTable.create(LootTableMother.lootTable().toDto())
        // When
        val (events, _) = createdLootTable.pullEvents()
        // Then
        events.shouldContainExactly(LootTableCreated(lootTableId = createdLootTable.toDto().id))
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val createdLootTable = LootTable.create(LootTableMother.lootTable().toDto())
        // When
        val (_, clearedLootTable) = createdLootTable.pullEvents()
        // Then
        clearedLootTable.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val lootTable = LootTableMother.lootTable()
        // When
        val (events, _) = lootTable.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
