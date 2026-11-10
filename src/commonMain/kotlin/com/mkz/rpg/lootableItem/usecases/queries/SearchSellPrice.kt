package com.mkz.rpg.lootableItem.usecases.queries

import com.mkz.rpg.lootableItem.domain.LootableItemRepository

class SearchSellPrice(
    private val lootableItemRepository: LootableItemRepository,
) {
    operator fun invoke(id: String): Int? = lootableItemRepository.searchById(id)?.sellPrice()
}
