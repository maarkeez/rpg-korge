package com.mkz.rpg.npc.domain

import com.mkz.rpg.npc.domain.NpcError.NpcLineRequired
import com.mkz.rpg.npc.domain.NpcError.NpcNameRequired
import com.mkz.rpg.npc.domain.NpcError.NpcNotRecruitable
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import com.mkz.rpg.npc.domain.NpcEvent.NpcRecruited
import kotlin.jvm.JvmInline

/**
 * A character standing on a tile of the battlefield, who says its line when talked to. It is no battle unit: it can not
 * be attacked. An npc with a unit is recruitable: it joins the party as that unit.
 */
@ConsistentCopyVisibility
data class Npc private constructor(
    private val id: Id,
    private val name: Name,
    private val line: Line,
    private val tile: Tile,
    private val unitId: UnitId?,
    private val events: Set<NpcEvent>,
) {
    companion object {
        fun place(dto: Dto): Npc =
            Npc(
                id = Id(dto.id),
                name = Name(dto.name),
                line = Line(dto.line),
                tile = Tile(row = dto.row, column = dto.column),
                unitId = dto.unitId?.let(::UnitId),
                events = setOf(NpcPlaced(npcId = dto.id, row = dto.row, column = dto.column, unitId = dto.unitId)),
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
            unitId = unitId?.value,
        )

    /** Whether it joins the party once talked to: it has a unit. */
    fun isRecruitable(): Boolean = unitId != null

    /** Recruits it, so it leaves its tile and joins the party as its unit. Fails when it is not recruitable. */
    fun recruit(): Npc {
        if (!isRecruitable()) throw NpcNotRecruitable()
        return copy(events = events + NpcRecruited(npcId = id.value, row = tile.row, column = tile.column))
    }

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

    @JvmInline private value class UnitId(
        val value: String,
    )

    private data class Tile(
        val row: Int,
        val column: Int,
    )

    /** Unit: the unit it joins the party as, or `null` when it is not recruitable. */
    data class Dto(
        val id: String,
        val name: String,
        val line: String,
        val row: Int,
        val column: Int,
        val unitId: String? = null,
    )
}
