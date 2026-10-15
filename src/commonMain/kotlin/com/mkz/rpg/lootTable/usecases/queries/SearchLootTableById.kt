package com.mkz.rpg.lootTable.usecases.queries

import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.domain.LootTableRepository

class SearchLootTableById(
    private val lootTableRepository: LootTableRepository,
) {
    operator fun invoke(id: String): LootTable.Dto? = lootTableRepository.searchById(id)?.toDto()
}
