package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.rpg.lootDrop.domain.LootDropError.DuplicatedLootDropLootTable
import com.mkz.rpg.lootDrop.domain.LootDropError.EmptyLootDropLootTables
import com.mkz.rpg.lootDrop.domain.LootDropEvent.DroppedItemPickedUp
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootTable.domain.LootTable
import kotlin.jvm.JvmInline
import kotlin.random.Random

/**
 * The loot tables a unit rolls on when one of its battle units is defeated, and the items it dropped, lying on their tiles until an inventory
 * stores them.
 */
@ConsistentCopyVisibility
data class LootDrop private constructor(
    private val unitId: UnitId,
    private val lootTableIds: LootTableIds,
    private val droppedItems: DroppedItems,
    private val events: Set<LootDropEvent>,
) {
    companion object {
        fun create(dto: Dto): LootDrop =
            LootDrop(
                unitId = UnitId(dto.unitId),
                lootTableIds = LootTableIds(dto.lootTableIds),
                droppedItems = DroppedItems.create(dto.droppedItems),
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

    /** Places a dropped item on its tile, e.g.: where the battle unit was defeated, where it lies until an inventory stores it. */
    fun place(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): LootDrop =
        copy(
            droppedItems = droppedItems.place(Position(row = row, column = column), LootableItemId(lootableItemId)),
            events = events + LootDropped(lootableItemId = lootableItemId, row = row, column = column),
        )

    /** The items dropped by the unit lying on the tile, in the order they were dropped. */
    fun droppedItems(
        row: Int,
        column: Int,
    ): List<String> = droppedItems.lootableItemIds(Position(row = row, column = column))

    /** Removes one copy of a dropped item from its tile, once an inventory has stored it. */
    fun pickUp(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): LootDrop =
        copy(
            droppedItems = droppedItems.pickUpOne(Position(row = row, column = column), LootableItemId(lootableItemId)),
            events = events + DroppedItemPickedUp(lootableItemId = lootableItemId, row = row, column = column),
        )

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
            if (value.isEmpty()) throw EmptyLootDropLootTables()
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

    /** The items lying on each tile, in the order they were dropped. A tile without items is left out. */
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

        fun place(
            position: Position,
            lootableItemId: LootableItemId,
        ) = DroppedItems(value + (position to (value[position].orEmpty() + lootableItemId)))

        fun pickUpOne(
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

    /** Dropped items: the items lying on each tile with items dropped by the unit, in the order they were dropped. */
    data class Dto(
        val unitId: String,
        val lootTableIds: List<String>,
        val droppedItems: Map<PositionDto, List<String>> = emptyMap(),
    ) {
        data class PositionDto(
            val row: Int,
            val column: Int,
        )
    }
}
