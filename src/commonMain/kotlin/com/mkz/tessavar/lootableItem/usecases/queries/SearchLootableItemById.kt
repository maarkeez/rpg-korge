package com.mkz.tessavar.lootableItem.usecases.queries

import com.mkz.tessavar.lootableItem.domain.LootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemRepository

class SearchLootableItemById(
    private val lootableItemRepository: LootableItemRepository,
) {
    operator fun invoke(id: String): LootableItem.Dto? = lootableItemRepository.searchById(id)?.toDto()
}
