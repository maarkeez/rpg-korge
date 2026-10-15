package com.mkz.rpg.lootDrop.adapters.storage

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

class InMemoryLootDropRepository : LootDropRepository {
    private val lootDrops = mutableMapOf<String, LootDrop>()

    override fun create(lootDrop: LootDrop) {
        lootDrops[lootDrop.toDto().unitId] = lootDrop
    }

    override fun searchByUnitId(unitId: String) = lootDrops[unitId]
}
