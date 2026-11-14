package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.Inventory.Dto.GoldReceiverDto
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.GiveRule

class SearchGoldReceivers(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
) {
    operator fun invoke(giverBattleUnitId: String): List<GoldReceiverDto> =
        giveRule.otherMemberIds(giverBattleUnitId).orEmpty().mapNotNull { memberId ->
            inventoryRepository
                .searchByBattleUnitId(memberId)
                ?.takeIf { inventory -> inventory.goldRoom() > 0 }
                ?.let { inventory -> GoldReceiverDto(battleUnitId = memberId, gold = inventory.toDto().gold, room = inventory.goldRoom()) }
        }
}
