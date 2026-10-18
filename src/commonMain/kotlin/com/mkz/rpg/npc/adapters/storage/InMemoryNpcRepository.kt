package com.mkz.rpg.npc.adapters.storage

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository

class InMemoryNpcRepository : NpcRepository {
    private val npcs = mutableMapOf<String, Npc>()

    override fun save(npc: Npc) {
        npcs[npc.toDto().id] = npc
    }

    override fun searchById(id: String) = npcs[id]

    override fun searchAll() = npcs.values.toList()
}
