package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.rpg.lootDrop.domain.LootDropError.DuplicatedLootDropLootTable
import com.mkz.rpg.lootDrop.domain.LootDropError.EmptyLootDropLootTables
import com.mkz.rpg.lootDrop.domain.LootDropEvent.DroppedItemLooted
import com.mkz.rpg.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.rpg.lootTable.domain.LootTable
import kotlin.jvm.JvmInline
import kotlin.random.Random

@ConsistentCopyVisibility
data class LootDrop private constructor(
    private val unitId: UnitId,
    private val lootTableIds: LootTableIds,
    private val droppedItems: DroppedItems,
    private val events: Set<LootDropEvent>,
) {
    companion object {
        const val OWNERLESS_UNIT_ID = "ownerless"

        fun create(dto: Dto): LootDrop {
            if (dto.lootTableIds.isEmpty()) throw EmptyLootDropLootTables()
            return LootDrop(
                unitId = UnitId(dto.unitId),
                lootTableIds = LootTableIds(dto.lootTableIds),
                droppedItems = DroppedItems.create(dto.droppedItems),
                events = setOf(LootDropCreated(unitId = dto.unitId)),
            )
        }

        fun ownerless(): LootDrop =
            LootDrop(
                unitId = UnitId(OWNERLESS_UNIT_ID),
                lootTableIds = LootTableIds(emptyList()),
                droppedItems = DroppedItems(emptyMap()),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun roll(
        lootTables: List<LootTable.Dto>,
        random: Random,
    ): String? {
        val entries = lootTables.flatMap(LootTable.Dto::entries)
        val roll = random.nextInt(entries.sumOf(LootTable.Dto.EntryDto::weight))
        var accumulatedWeight = 0
        val rolledEntry =
            entries.first { entry ->
                accumulatedWeight += entry.weight
                roll < accumulatedWeight
            }
        return rolledEntry.lootableItemId
    }

    fun drop(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): LootDrop =
        copy(
            droppedItems = droppedItems.drop(Position(row = row, column = column), LootableItemId(lootableItemId)),
            events = events + ItemDropped(lootableItemId = lootableItemId, row = row, column = column),
        )

    fun droppedItems(
        row: Int,
        column: Int,
    ): List<String> = droppedItems.lootableItemIds(Position(row = row, column = column))

    fun loot(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): LootDrop =
        copy(
            droppedItems = droppedItems.lootOne(Position(row = row, column = column), LootableItemId(lootableItemId)),
            events = events + DroppedItemLooted(lootableItemId = lootableItemId, row = row, column = column),
        )

    fun loadDroppedItems(droppedItems: Map<Dto.PositionDto, List<String>>): LootDrop = copy(droppedItems = DroppedItems.create(droppedItems))

    fun isOwnerlessAndEmpty(): Boolean = unitId.value == OWNERLESS_UNIT_ID && droppedItems.value.isEmpty()

    fun toDto() =
        Dto(
            unitId = unitId.value,
            lootTableIds = lootTableIds.value,
            droppedItems = droppedItems.toDto(),
        )

    @JvmInline private value class UnitId(
        val value: String,
    )

    @JvmInline private value class LootTableIds(
        val value: List<String>,
    ) {
        init {
            if (value.size != value.toSet().size) throw DuplicatedLootDropLootTable()
        }
    }

    private data class Position(
        val row: Int,
        val column: Int,
    )

    @JvmInline private value class LootableItemId(
        val value: String,
    )

    @JvmInline private value class DroppedItems(
        val value: Map<Position, List<LootableItemId>>,
    ) {
        companion object {
            fun create(droppedItems: Map<PositionDto, List<String>>) =
                DroppedItems(
                    droppedItems
                        .filterValues { lootableItemIds -> lootableItemIds.isNotEmpty() }
                        .map { (position, lootableItemIds) -> Position(row = position.row, column = position.column) to lootableItemIds.map(::LootableItemId) }
                        .toMap(),
                )
        }

        fun drop(
            position: Position,
            lootableItemId: LootableItemId,
        ) = DroppedItems(value + (position to (value[position].orEmpty() + lootableItemId)))

        fun lootOne(
            position: Position,
            lootableItemId: LootableItemId,
        ): DroppedItems {
            val lootableItemIds = value[position].orEmpty()
            val index = lootableItemIds.indexOf(lootableItemId)
            if (index < 0) throw DroppedItemNotFound()
            val leftItems = lootableItemIds.filterIndexed { droppedIndex, _ -> droppedIndex != index }
            return DroppedItems(if (leftItems.isEmpty()) value - position else value + (position to leftItems))
        }

        fun lootableItemIds(position: Position): List<String> = value[position].orEmpty().map(LootableItemId::value)

        fun toDto(): Map<PositionDto, List<String>> =
            value.map { (position, lootableItemIds) -> PositionDto(row = position.row, column = position.column) to lootableItemIds.map(LootableItemId::value) }.toMap()
    }

    data class Dto(
        val unitId: String,
        val lootTableIds: List<String>,
        val droppedItems: Map<PositionDto, List<String>>,
    ) {
        data class PositionDto(
            val row: Int,
            val column: Int,
        )
    }
}
