package com.mkz.rpg.lootableItem.usecases.services

import com.mkz.rpg.lootableItem.domain.LootableItem

// Loads the given lootable items, instead of reading them from the resources.
class FakeLootableItemLoader(
    private val lootableItems: List<LootableItem.Dto> = emptyList(),
) : LootableItemLoader {
    override fun loadLootableItems(): List<LootableItem.Dto> = lootableItems
}
