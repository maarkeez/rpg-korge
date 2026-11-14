package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.GiveRule

class SearchItemReceivers(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
) {
    operator fun invoke(giverBattleUnitId: String): List<String> =
        giveRule.otherMemberIds(giverBattleUnitId).orEmpty().filter { memberId ->
            inventoryRepository.searchByBattleUnitId(memberId)?.canStore() ?: false
        }
}
