package com.mkz.tessavar.lootableItem.usecases.services

import com.mkz.tessavar.lootableItem.domain.LootableItem

interface LootableItemLoader {
    suspend fun initResources() {}

    fun loadLootableItems(): List<LootableItem.Dto>
}
