package com.mkz.rpg.npc.domain

import korlibs.io.util.UUID

object NpcMother {
    fun npc(
        id: String = id(),
        row: Int = 0,
        column: Int = 0,
        unitId: String? = null,
        stock: List<String> = emptyList(),
    ): Npc = Npc.place(Npc.Dto(id = id, row = row, column = column, unitId = unitId, stock = stock)).pullEvents().second

    fun id() = "npc-${UUID.randomUUID()}"
}
