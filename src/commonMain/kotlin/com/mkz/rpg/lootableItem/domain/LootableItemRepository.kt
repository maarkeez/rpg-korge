package com.mkz.rpg.lootableItem.domain

interface LootableItemRepository {
    fun create(lootableItem: LootableItem)

    fun searchById(id: String): LootableItem?
}
