package com.mkz.tessavar.npc.usecases.queries

import com.mkz.tessavar.npc.domain.NpcRepository

class SearchIsMerchant(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(npcId: String): Boolean = npcRepository.searchById(npcId)?.isMerchant() == true
}
