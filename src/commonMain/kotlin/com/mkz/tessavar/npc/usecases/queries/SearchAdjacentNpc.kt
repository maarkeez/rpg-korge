package com.mkz.tessavar.npc.usecases.queries

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcRepository

class SearchAdjacentNpc(
    private val npcRepository: NpcRepository,
) {
    private val northEastSouthWestOffsets = listOf(-1 to 0, 0 to 1, 1 to 0, 0 to -1)

    operator fun invoke(
        row: Int,
        column: Int,
    ): Npc.Dto? {
        val npcs = npcRepository.searchAll()
        return northEastSouthWestOffsets
            .firstNotNullOfOrNull { (rowOffset, columnOffset) ->
                npcs.firstOrNull { npc -> npc.isAt(row = row + rowOffset, column = column + columnOffset) }
            }?.toDto()
    }
}
