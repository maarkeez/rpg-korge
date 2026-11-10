package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

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
