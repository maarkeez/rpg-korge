package com.mkz.tessavar.lootTable.usecases.services

import com.mkz.tessavar.lootTable.domain.LootTable

class FakeLootTableLoader(
    private val lootTables: List<LootTable.Dto> = emptyList(),
) : LootTableLoader {
    override fun loadLootTables(): List<LootTable.Dto> = lootTables
}
