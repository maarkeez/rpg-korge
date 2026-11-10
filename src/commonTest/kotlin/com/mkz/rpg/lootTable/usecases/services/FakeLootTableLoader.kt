package com.mkz.rpg.lootTable.usecases.services

import com.mkz.rpg.lootTable.domain.LootTable

class FakeLootTableLoader(
    private val lootTables: List<LootTable.Dto> = emptyList(),
) : LootTableLoader {
    override fun loadLootTables(): List<LootTable.Dto> = lootTables
}
