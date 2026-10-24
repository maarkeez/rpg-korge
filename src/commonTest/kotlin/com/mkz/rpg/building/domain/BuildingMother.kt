package com.mkz.rpg.building.domain

import korlibs.io.util.UUID

object BuildingMother {
    fun building(
        id: String = id(),
        row: Int = 0,
        column: Int = 0,
        rows: Int = 1,
        columns: Int = 1,
    ): Building = Building.place(Building.Dto(id = id, row = row, column = column, rows = rows, columns = columns)).pullEvents().second

    fun id() = "building-${UUID.randomUUID()}"
}
