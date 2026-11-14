package com.mkz.tessavar.lootTable.usecases.queries

import com.mkz.tessavar.lootTable.domain.LootTable
import com.mkz.tessavar.lootTable.domain.LootTableRepository

class SearchLootTableById(
    private val lootTableRepository: LootTableRepository,
) {
    operator fun invoke(id: String): LootTable.Dto? = lootTableRepository.searchById(id)?.toDto()
}
