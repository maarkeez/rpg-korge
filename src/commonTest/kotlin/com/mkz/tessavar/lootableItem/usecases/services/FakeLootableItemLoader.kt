package com.mkz.tessavar.lootableItem.usecases.services

import com.mkz.tessavar.lootableItem.domain.LootableItem

class FakeLootableItemLoader(
    private val lootableItems: List<LootableItem.Dto> = emptyList(),
) : LootableItemLoader {
    override fun loadLootableItems(): List<LootableItem.Dto> = lootableItems
}
