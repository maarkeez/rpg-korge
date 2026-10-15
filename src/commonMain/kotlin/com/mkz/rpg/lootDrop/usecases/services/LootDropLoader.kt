package com.mkz.rpg.lootDrop.usecases.services

import com.mkz.rpg.lootDrop.domain.LootDrop

interface LootDropLoader {
    suspend fun initResources() {}

    fun loadLootDrops(): List<LootDrop.Dto>
}
