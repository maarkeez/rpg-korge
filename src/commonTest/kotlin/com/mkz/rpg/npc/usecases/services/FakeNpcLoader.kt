package com.mkz.rpg.npc.usecases.services

import com.mkz.rpg.npc.domain.Npc

// Loads the given npcs, instead of reading them from the resources.
class FakeNpcLoader(
    private val npcs: List<Npc.Dto> = emptyList(),
) : NpcLoader {
    override fun loadNpcs(): List<Npc.Dto> = npcs
}
