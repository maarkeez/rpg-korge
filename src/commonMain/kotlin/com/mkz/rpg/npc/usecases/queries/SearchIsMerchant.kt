package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.NpcRepository

/** Whether the npc shows the items it sells once talked to. `false` when it does not exist. */
class SearchIsMerchant(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(npcId: String): Boolean = npcRepository.searchById(npcId)?.isMerchant() == true
}
