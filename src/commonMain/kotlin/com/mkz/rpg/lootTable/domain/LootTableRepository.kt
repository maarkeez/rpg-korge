package com.mkz.rpg.lootTable.domain

interface LootTableRepository {
    fun create(lootTable: LootTable)

    fun searchById(id: String): LootTable?
}
