package com.mkz.rpg.lootableItem.usecases.services

import com.mkz.rpg.lootableItem.domain.LootableItem

class FakeLootableItemLoader(
    private val lootableItems: List<LootableItem.Dto> = emptyList(),
) : LootableItemLoader {
    override fun loadLootableItems(): List<LootableItem.Dto> = lootableItems
}
