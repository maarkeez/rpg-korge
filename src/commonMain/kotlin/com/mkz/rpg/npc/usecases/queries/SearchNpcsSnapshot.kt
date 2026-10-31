package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

/** What is saved of the npcs left on the battlefield, with their tiles and stocks: a recruited npc is gone, as it joined the party. */
class SearchNpcsSnapshot(
    private val npcRepository: NpcRepository,
) {
    operator fun invoke(): List<Npc.Dto> = npcRepository.searchAll().map(Npc::toDto)
}
