package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

class SearchNpcById(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(npcId: String): Npc.Dto? = npcRepository.searchById(npcId)?.toDto()
}
