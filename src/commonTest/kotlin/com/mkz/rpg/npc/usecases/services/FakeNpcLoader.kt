package com.mkz.rpg.npc.usecases.services

import com.mkz.rpg.npc.domain.Npc

class FakeNpcLoader(
    private val npcs: List<Npc.Dto> = emptyList(),
) : NpcLoader {
    override fun loadNpcs(): List<Npc.Dto> = npcs
}
