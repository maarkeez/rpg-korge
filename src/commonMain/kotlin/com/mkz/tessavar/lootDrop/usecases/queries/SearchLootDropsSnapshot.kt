package com.mkz.tessavar.lootDrop.usecases.queries

import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.domain.LootDropRepository

class SearchLootDropsSnapshot(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(): List<LootDrop.Dto> = lootDropRepository.searchAll().filterNot(LootDrop::isOwnerlessAndEmpty).map(LootDrop::toDto)
}
