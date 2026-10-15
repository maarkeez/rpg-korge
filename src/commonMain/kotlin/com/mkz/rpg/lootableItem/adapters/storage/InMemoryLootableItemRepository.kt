package com.mkz.rpg.lootableItem.adapters.storage

import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemRepository

class InMemoryLootableItemRepository : LootableItemRepository {
    private val lootableItems = mutableMapOf<String, LootableItem>()

    override fun create(lootableItem: LootableItem) {
        lootableItems[lootableItem.toDto().id] = lootableItem
    }

    override fun searchById(id: String) = lootableItems[id]
}
