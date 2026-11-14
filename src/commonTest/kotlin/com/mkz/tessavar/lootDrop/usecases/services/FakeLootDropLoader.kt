package com.mkz.tessavar.lootDrop.usecases.services

import com.mkz.tessavar.lootDrop.domain.LootDrop

class FakeLootDropLoader(
    private val lootDrops: List<LootDrop.Dto> = emptyList(),
) : LootDropLoader {
    override fun loadLootDrops(): List<LootDrop.Dto> = lootDrops
}
