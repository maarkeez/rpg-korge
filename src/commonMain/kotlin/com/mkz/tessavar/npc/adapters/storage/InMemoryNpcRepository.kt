package com.mkz.tessavar.npc.adapters.storage

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcRepository

class InMemoryNpcRepository : NpcRepository {
    private val npcs = mutableMapOf<String, Npc>()

    override fun save(npc: Npc) {
        npcs[npc.toDto().id] = npc
    }

    override fun searchById(id: String) = npcs[id]

    override fun searchAll() = npcs.values.toList()

    override fun delete(id: String) {
        npcs.remove(id)
    }
}
