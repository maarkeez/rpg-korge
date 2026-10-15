package com.mkz.rpg.lootableItem.usecases.queries

import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemRepository

class SearchLootableItemById(
    private val lootableItemRepository: LootableItemRepository,
) {
    operator fun invoke(id: String): LootableItem.Dto? = lootableItemRepository.searchById(id)?.toDto()
}
