package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.NpcRepository

class SearchIsMerchant(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(npcId: String): Boolean = npcRepository.searchById(npcId)?.isMerchant() == true
}
