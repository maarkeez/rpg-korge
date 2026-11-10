package com.mkz.rpg.building.domain

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.rpg.building.domain.BuildingError.BuildingSizeNotPositive
import com.mkz.rpg.building.domain.BuildingEvent.BuildingPlaced
import com.mkz.rpg.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Building private constructor(
    private val id: Id,
    private val area: Area,
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

    data class Dto(
        val id: String,
        val row: Int,
        val column: Int,
        val rows: Int,
        val columns: Int,
    )
}
