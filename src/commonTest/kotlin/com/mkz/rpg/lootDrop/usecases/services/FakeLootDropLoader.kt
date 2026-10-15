package com.mkz.rpg.lootDrop.usecases.services

import com.mkz.rpg.lootDrop.domain.LootDrop

// Loads the given loot drops, instead of reading them from the resources.
class FakeLootDropLoader(
    private val lootDrops: List<LootDrop.Dto> = emptyList(),
) : LootDropLoader {
    override fun loadLootDrops(): List<LootDrop.Dto> = lootDrops
}
