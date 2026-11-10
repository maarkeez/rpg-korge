package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

class SearchItemReceivers(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
) {
    operator fun invoke(giverBattleUnitId: String): List<String> =
        giveRule.otherMemberIds(giverBattleUnitId).orEmpty().filter { memberId ->
            inventoryRepository.searchByBattleUnitId(memberId)?.canStore() ?: false
        }
}
