package com.mkz.tessavar.lootableItem.adapters.storage

import com.mkz.tessavar.lootableItem.domain.LootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemRepository

class InMemoryLootableItemRepository : LootableItemRepository {
    private val lootableItems = mutableMapOf<String, LootableItem>()

    override fun create(lootableItem: LootableItem) {
        lootableItems[lootableItem.toDto().id] = lootableItem
    }

    override fun searchById(id: String) = lootableItems[id]
}
