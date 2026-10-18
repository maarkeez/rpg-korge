package com.mkz.rpg.npc.usecases.services

import com.mkz.rpg.npc.domain.Npc

interface NpcLoader {
    suspend fun initResources() {}

    fun loadNpcs(): List<Npc.Dto>
}
