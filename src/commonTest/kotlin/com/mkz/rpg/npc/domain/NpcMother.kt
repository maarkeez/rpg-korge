package com.mkz.rpg.npc.domain

import korlibs.io.util.UUID

object NpcMother {
    fun npc(
        id: String = id(),
        name: String = "The Guy",
        line: String = "G'day bloke!",
        row: Int = 0,
        column: Int = 0,
    ): Npc = Npc.place(Npc.Dto(id = id, name = name, line = line, row = row, column = column)).pullEvents().second

    fun id() = "npc-${UUID.randomUUID()}"
}
