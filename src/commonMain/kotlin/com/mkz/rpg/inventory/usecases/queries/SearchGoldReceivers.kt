package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GoldReceiverDto
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

/** The members of the exploring party, the giver excluded, that have room for more gold, in party order. */
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
