package com.mkz.tessavar.npc.usecases.services

import com.mkz.tessavar.npc.domain.Npc

interface NpcLoader {
    suspend fun initResources() {}

    fun loadNpcs(): List<Npc.Dto>
}
