package com.mkz.rpg.lootTable.usecases.services

import com.mkz.rpg.lootTable.domain.LootTable

interface LootTableLoader {
    suspend fun initResources() {}

    fun loadLootTables(): List<LootTable.Dto>
}
