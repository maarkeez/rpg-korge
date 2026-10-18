package com.mkz.rpg.npc.domain

import com.mkz.rpg.npc.domain.NpcError.NpcLineRequired
import com.mkz.rpg.npc.domain.NpcError.NpcNameRequired
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import kotlin.jvm.JvmInline

/** A character standing on a tile of the battlefield, who says its line when talked to. It is no battle unit: it can not be attacked. */
@ConsistentCopyVisibility
data class Npc private constructor(
    private val id: Id,
    private val name: Name,
    private val line: Line,
    private val tile: Tile,
    private val events: Set<NpcEvent>,
) {
    companion object {
        fun place(dto: Dto): Npc =
            Npc(
                id = Id(dto.id),
                name = Name(dto.name),
                line = Line(dto.line),
                tile = Tile(row = dto.row, column = dto.column),
                events = setOf(NpcPlaced(npcId = dto.id, row = dto.row, column = dto.column)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            name = name.value,
            line = line.value,
            row = tile.row,
            column = tile.column,
        )

    fun isAt(
        row: Int,
        column: Int,
    ): Boolean = tile.row == row && tile.column == column

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class Name(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw NpcNameRequired()
        }
    }

    @JvmInline private value class Line(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw NpcLineRequired()
        }
    }

    private data class Tile(
        val row: Int,
        val column: Int,
    )

    data class Dto(
        val id: String,
        val name: String,
        val line: String,
        val row: Int,
        val column: Int,
    )
}
