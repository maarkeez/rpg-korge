package com.mkz.rpg.lootableItem.usecases.services

import com.mkz.rpg.lootableItem.domain.LootableItem

interface LootableItemLoader {
    suspend fun initResources() {}

    fun loadLootableItems(): List<LootableItem.Dto>
}
