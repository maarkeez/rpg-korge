package com.mkz.rpg.npc.domain

interface NpcRepository {
    fun save(npc: Npc)

    fun searchById(id: String): Npc?

    fun searchAll(): List<Npc>
}
