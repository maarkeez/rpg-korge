package com.mkz.tessavar.npc.usecases.services

import com.mkz.tessavar.npc.domain.Npc

class FakeNpcLoader(
    private val npcs: List<Npc.Dto> = emptyList(),
) : NpcLoader {
    override fun loadNpcs(): List<Npc.Dto> = npcs
}
