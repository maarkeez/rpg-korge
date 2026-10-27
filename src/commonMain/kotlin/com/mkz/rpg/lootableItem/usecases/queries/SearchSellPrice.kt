package com.mkz.rpg.lootableItem.usecases.queries

import com.mkz.rpg.lootableItem.domain.LootableItemRepository

/** What a merchant pays for the lootable item: half its price, rounded down. `null` when it does not exist. */
class SearchSellPrice(
    private val lootableItemRepository: LootableItemRepository,
) {
    operator fun invoke(id: String): Int? = lootableItemRepository.searchById(id)?.sellPrice()
}
