package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

/** What is saved of the loot drops: the items lying on the battlefield, by the unit that dropped them. The ownerless loot drop is left out while it holds nothing. */
class SearchLootDropsSnapshot(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(): List<LootDrop.Dto> = lootDropRepository.searchAll().filterNot(LootDrop::isOwnerlessAndEmpty).map(LootDrop::toDto)
}
