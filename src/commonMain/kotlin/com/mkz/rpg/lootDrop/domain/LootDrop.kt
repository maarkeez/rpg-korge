package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.lootDrop.domain.LootDropError.DuplicatedLootDropLootTable
import com.mkz.rpg.lootDrop.domain.LootDropError.EmptyLootDropLootTables
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.rpg.lootTable.domain.LootTable
import kotlin.jvm.JvmInline
import kotlin.random.Random

/** The loot tables a unit rolls on when one of its battle units is defeated. */
@ConsistentCopyVisibility
data class LootDrop private constructor(
    private val unitId: UnitId,
    private val lootTableIds: LootTableIds,
    private val events: Set<LootDropEvent>,
) {
    companion object {
        fun create(dto: Dto): LootDrop =
            LootDrop(
                unitId = UnitId(dto.unitId),
                lootTableIds = LootTableIds(dto.lootTableIds),
                events = setOf(LootDropCreated(unitId = dto.unitId)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    /**
     * Picks the lootable item to drop, or `null` when nothing drops, from the entries of the given [lootTables] merged together,
     * in proportion to their weights.
     */
    fun drop(
        lootTables: List<LootTable.Dto>,
        random: Random,
    ): String? {
        val entries = lootTables.flatMap(LootTable.Dto::entries)
        val roll = random.nextInt(entries.sumOf(LootTable.Dto.EntryDto::weight))
        var accumulatedWeight = 0
        val droppedEntry =
            entries.first { entry ->
                accumulatedWeight += entry.weight
                roll < accumulatedWeight
            }
        return droppedEntry.lootableItemId
    }

    fun toDto() =
        Dto(
            unitId = unitId.value,
            lootTableIds = lootTableIds.value,
        )

    @JvmInline private value class UnitId(
        val value: String,
    )

    @JvmInline private value class LootTableIds(
        val value: List<String>,
    ) {
        init {
            if (value.isEmpty()) throw EmptyLootDropLootTables()
            if (value.size != value.toSet().size) throw DuplicatedLootDropLootTable()
        }
    }

    data class Dto(
        val unitId: String,
        val lootTableIds: List<String>,
    )
}
