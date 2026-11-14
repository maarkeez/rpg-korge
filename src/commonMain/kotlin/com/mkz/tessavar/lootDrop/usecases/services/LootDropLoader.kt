package com.mkz.tessavar.lootDrop.usecases.services

import com.mkz.tessavar.lootDrop.domain.LootDrop

interface LootDropLoader {
    suspend fun initResources() {}

    fun loadLootDrops(): List<LootDrop.Dto>
}
