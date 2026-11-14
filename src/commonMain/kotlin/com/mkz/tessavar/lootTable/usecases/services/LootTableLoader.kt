package com.mkz.tessavar.lootTable.usecases.services

import com.mkz.tessavar.lootTable.domain.LootTable

interface LootTableLoader {
    suspend fun initResources() {}

    fun loadLootTables(): List<LootTable.Dto>
}
