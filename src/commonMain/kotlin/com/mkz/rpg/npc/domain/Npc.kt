package com.mkz.rpg.npc.domain

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.rpg.npc.domain.NpcError.NpcNotRecruitable
import com.mkz.rpg.npc.domain.NpcError.NpcStockItemRepeated
import com.mkz.rpg.npc.domain.NpcError.NpcStockTooLarge
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import com.mkz.rpg.npc.domain.NpcEvent.NpcRecruited
import com.mkz.rpg.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

/**
 * A character standing on a tile of the battlefield, who says its line when talked to. It is no battle unit: it can not
 * be attacked. An npc with a unit is recruitable: it joins the party as that unit. An npc with a stock is a merchant: it
 * shows the lootable items it sells.
 */
@ConsistentCopyVisibility
data class Npc private constructor(
    private val id: Id,
    private val tile: Tile,
    private val unitId: UnitId?,
    private val stock: Stock,
    // Its own events, each followed by the battlefield request it needs (e.g.: occupying the tile it was placed on),
    // published together so the battlefield is up to date before anything reacting to the next events.
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

        /** Rebuilds the npc as it was when the game was saved, from its [toDto], publishing no events. */
        fun restore(dto: Dto): Npc =
            Npc(
                id = Id(dto.id),
                tile = Tile(row = dto.row, column = dto.column),
                unitId = dto.unitId?.let(::UnitId),
                stock = Stock(dto.stock),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            row = tile.row,
            column = tile.column,
            unitId = unitId?.value,
            stock = stock.lootableItemIds,
        )

    /** Whether it joins the party once talked to: it has a unit. */
    fun isRecruitable(): Boolean = unitId != null

    /** Whether it shows the items it sells once talked to: it has a stock. */
    fun isMerchant(): Boolean = stock.lootableItemIds.isNotEmpty()

    /** Recruits it, so it leaves its tile and joins the party as its unit. Fails when it is not recruitable. */
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

    /**
     * Unit: the unit it joins the party as, or `null` when it is not recruitable. Stock: the lootable items it sells, in
     * the order they are shown, or none when it is no merchant.
     */
    data class Dto(
        val id: String,
        val row: Int,
        val column: Int,
        val unitId: String?,
        val stock: List<String>,
    )
}
