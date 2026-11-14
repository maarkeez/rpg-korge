package com.mkz.tessavar.lootTable.adapters.storage

import com.mkz.tessavar.lootTable.domain.LootTable
import com.mkz.tessavar.lootTable.domain.LootTableRepository

class InMemoryLootTableRepository : LootTableRepository {
    private val lootTables = mutableMapOf<String, LootTable>()

    override fun create(lootTable: LootTable) {
        lootTables[lootTable.toDto().id] = lootTable
    }

    override fun searchById(id: String) = lootTables[id]
}
