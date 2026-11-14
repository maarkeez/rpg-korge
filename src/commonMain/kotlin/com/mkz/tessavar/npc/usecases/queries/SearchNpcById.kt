package com.mkz.tessavar.npc.usecases.queries

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcRepository

class SearchNpcById(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(npcId: String): Npc.Dto? = npcRepository.searchById(npcId)?.toDto()
}
