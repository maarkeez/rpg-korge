package com.mkz.tessavar.lootDrop.adapters.storage

import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.domain.LootDropRepository

class InMemoryLootDropRepository : LootDropRepository {
    private val lootDrops = mutableMapOf<String, LootDrop>()

    override fun create(lootDrop: LootDrop) {
        lootDrops[lootDrop.toDto().unitId] = lootDrop
    }

    override fun update(lootDrop: LootDrop) {
        lootDrops[lootDrop.toDto().unitId] = lootDrop
    }

    override fun searchByUnitId(unitId: String) = lootDrops[unitId]

    override fun searchAll() = lootDrops.values.toList()
}
