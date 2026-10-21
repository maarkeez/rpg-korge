package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

/**
 * The first npc standing next to the tile, looking north, east, south and west, in that order: no diagonals. `null`
 * when there is none.
 */
class SearchNpcNextTo(
    private val npcRepository: NpcRepository,
) {
    // Row and column offsets of the tiles next to it, in the order they are looked at.
    private val adjacentOffsets = listOf(-1 to 0, 0 to 1, 1 to 0, 0 to -1)

    operator fun invoke(
        row: Int,
        column: Int,
    ): Npc.Dto? {
        val npcs = npcRepository.searchAll()
        return adjacentOffsets
            .firstNotNullOfOrNull { (rowOffset, columnOffset) ->
                npcs.firstOrNull { npc -> npc.isAt(row = row + rowOffset, column = column + columnOffset) }
            }?.toDto()
    }
}
