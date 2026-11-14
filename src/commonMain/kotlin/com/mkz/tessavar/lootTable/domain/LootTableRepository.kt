package com.mkz.tessavar.lootTable.domain

interface LootTableRepository {
    fun create(lootTable: LootTable)

    fun searchById(id: String): LootTable?
}
