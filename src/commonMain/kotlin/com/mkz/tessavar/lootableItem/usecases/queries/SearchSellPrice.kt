package com.mkz.tessavar.lootableItem.usecases.queries

import com.mkz.tessavar.lootableItem.domain.LootableItemRepository

class SearchSellPrice(
    private val lootableItemRepository: LootableItemRepository,
) {
    operator fun invoke(id: String): Int? = lootableItemRepository.searchById(id)?.sellPrice()
}
