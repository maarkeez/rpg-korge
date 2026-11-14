package com.mkz.tessavar.npc.domain

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.tessavar.npc.domain.NpcError.NpcNotRecruitable
import com.mkz.tessavar.npc.domain.NpcError.NpcStockItemRepeated
import com.mkz.tessavar.npc.domain.NpcError.NpcStockTooLarge
import com.mkz.tessavar.npc.domain.NpcEvent.NpcPlaced
import com.mkz.tessavar.npc.domain.NpcEvent.NpcRecruited
import com.mkz.tessavar.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Npc private constructor(
    private val id: Id,
    private val tile: Tile,
    private val unitId: UnitId?,
    private val stock: Stock,
    private val events: Set<DomainEvent>,
) {
    companion object {
        const val MAX_STOCK_SIZE = 12

        fun place(dto: Dto): Npc =
            Npc(
                id = Id(dto.id),
                tile = Tile(row = dto.row, column = dto.column),
                unitId = dto.unitId?.let(::UnitId),
                stock = Stock(dto.stock),
                events =
                    setOf(
                        NpcPlaced(npcId = dto.id, row = dto.row, column = dto.column, unitId = dto.unitId),
                        RequestOccupyTile(occupantId = dto.id, row = dto.row, column = dto.column),
                    ),
            )

        fun load(dto: Dto): Npc =
            Npc(
                id = Id(dto.id),
                tile = Tile(row = dto.row, column = dto.column),
                unitId = dto.unitId?.let(::UnitId),
                stock = Stock(dto.stock),
                events = emptySet(),
            )
    }

    fun pullEvents(): Pair<Set<DomainEvent>, Npc> = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            row = tile.row,
            column = tile.column,
            unitId = unitId?.value,
            stock = stock.lootableItemIds,
        )

    fun isRecruitable(): Boolean = unitId != null

    fun isMerchant(): Boolean = stock.lootableItemIds.isNotEmpty()

    fun recruit(): Npc {
        if (!isRecruitable()) throw NpcNotRecruitable()
        return copy(
            events =
                events +
                    NpcRecruited(npcId = id.value, row = tile.row, column = tile.column) +
                    RequestRemoveOccupant(occupantId = id.value),
        )
    }

    fun isAt(
        row: Int,
        column: Int,
    ): Boolean = tile.row == row && tile.column == column

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class UnitId(
        val value: String,
    )

    @JvmInline private value class Stock(
        val lootableItemIds: List<String>,
    ) {
        init {
            if (lootableItemIds.size > MAX_STOCK_SIZE) throw NpcStockTooLarge()
            if (lootableItemIds.distinct().size != lootableItemIds.size) throw NpcStockItemRepeated()
        }
    }

    private data class Tile(
        val row: Int,
        val column: Int,
    )

    data class Dto(
        val id: String,
        val row: Int,
        val column: Int,
        val unitId: String?,
        val stock: List<String>,
    )
}
