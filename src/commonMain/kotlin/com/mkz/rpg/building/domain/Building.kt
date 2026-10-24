package com.mkz.rpg.building.domain

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.rpg.building.domain.BuildingError.BuildingSizeNotPositive
import com.mkz.rpg.building.domain.BuildingEvent.BuildingPlaced
import com.mkz.rpg.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

/**
 * A building standing on several tiles of the battlefield, e.g.: a fortress. It is no battle unit nor npc: it blocks
 * all its tiles for everyone, and can not be attacked nor talked to.
 */
@ConsistentCopyVisibility
data class Building private constructor(
    private val id: Id,
    private val area: Area,
    // Its own events, each followed by the battlefield request it needs (e.g.: occupying the tiles it was placed on),
    // published together so the battlefield is up to date before anything reacting to the next events.
    private val events: Set<DomainEvent>,
) {
    companion object {
        fun place(dto: Dto): Building =
            Building(
                id = Id(dto.id),
                area = Area(row = dto.row, column = dto.column, rows = dto.rows, columns = dto.columns),
                events =
                    setOf(
                        BuildingPlaced(buildingId = dto.id, row = dto.row, column = dto.column),
                        RequestOccupyTiles(occupantId = dto.id, row = dto.row, column = dto.column, rows = dto.rows, columns = dto.columns),
                    ),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            row = area.row,
            column = area.column,
            rows = area.rows,
            columns = area.columns,
        )

    @JvmInline private value class Id(
        val value: String,
    )

    // From its top-left tile, this many rows and columns.
    private data class Area(
        val row: Int,
        val column: Int,
        val rows: Int,
        val columns: Int,
    ) {
        init {
            if (rows < 1 || columns < 1) throw BuildingSizeNotPositive()
        }
    }

    /** Row and column: its top-left tile. Rows and columns: its size, in tiles. */
    data class Dto(
        val id: String,
        val row: Int,
        val column: Int,
        val rows: Int,
        val columns: Int,
    )
}
