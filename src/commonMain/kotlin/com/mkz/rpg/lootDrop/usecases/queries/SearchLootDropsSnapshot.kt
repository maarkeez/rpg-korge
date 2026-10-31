package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

/** What is saved of the loot drops: the items lying on the battlefield, by the unit that dropped them. */
class SearchLootDropsSnapshot(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(): List<LootDrop.Dto> = lootDropRepository.searchAll().map(LootDrop::toDto)
}
