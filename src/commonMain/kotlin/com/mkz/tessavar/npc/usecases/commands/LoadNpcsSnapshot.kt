package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcRepository

class LoadNpcsSnapshot(
    private val npcRepository: NpcRepository,
    private val searchLootableItemById: SearchLootableItemById,
) {
    operator fun invoke(npcs: List<Npc.Dto>) {
        npcs.flatMap(Npc.Dto::stock).forEach { lootableItemId -> requireNotNull(searchLootableItemById(lootableItemId)) { "Unknown lootable item $lootableItemId" } }
        npcRepository.searchAll().forEach { npc -> npcRepository.delete(npc.toDto().id) }
        npcs.forEach { npc -> npcRepository.save(Npc.load(npc)) }
    }
}
