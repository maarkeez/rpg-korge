package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

class SearchLootDropsSnapshot(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(): List<LootDrop.Dto> = lootDropRepository.searchAll().filterNot(LootDrop::isOwnerlessAndEmpty).map(LootDrop::toDto)
}
