package com.mkz.tessavar.npc.usecases.queries

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcRepository

class SearchNpcsSnapshot(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(): List<Npc.Dto> = npcRepository.searchAll().map(Npc::toDto)
}
