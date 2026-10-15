package com.mkz.rpg.lootTable.adapters.storage

import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.domain.LootTableRepository

class InMemoryLootTableRepository : LootTableRepository {
    private val lootTables = mutableMapOf<String, LootTable>()

    override fun create(lootTable: LootTable) {
        lootTables[lootTable.toDto().id] = lootTable
    }

    override fun searchById(id: String) = lootTables[id]
}
