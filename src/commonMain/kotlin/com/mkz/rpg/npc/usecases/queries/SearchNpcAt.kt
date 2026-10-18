package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

/** The npc standing on the tile, or `null` when there is none. */
class SearchNpcAt(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Npc.Dto? = npcRepository.searchAll().firstOrNull { npc -> npc.isAt(row, column) }?.toDto()
}
