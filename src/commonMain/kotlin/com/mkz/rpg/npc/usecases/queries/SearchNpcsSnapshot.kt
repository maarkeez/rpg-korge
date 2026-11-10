package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

class SearchNpcsSnapshot(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(): List<Npc.Dto> = npcRepository.searchAll().map(Npc::toDto)
}
