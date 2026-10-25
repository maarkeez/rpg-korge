package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

/** The members of the exploring party, the giver excluded, that have space left to receive an item, in party order. */
class SearchGiveReceivers(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
) {
    operator fun invoke(giverBattleUnitId: String): List<String> =
        giveRule.otherMemberIds(giverBattleUnitId).orEmpty().filter { memberId ->
            inventoryRepository.searchByBattleUnitId(memberId)?.canStore() ?: false
        }
}
