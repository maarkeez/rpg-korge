package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveItemRefusalDto
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

class SearchGiveItemRefusal(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
    private val searchItemReceivers: SearchItemReceivers,
) {
    operator fun invoke(
        giverBattleUnitId: String,
        lootableItemId: String,
    ): GiveItemRefusalDto? {
        val otherMemberIds = giveRule.otherMemberIds(giverBattleUnitId) ?: return GiveItemRefusalDto.NOT_EXPLORING
        val inventory = inventoryRepository.searchByBattleUnitId(giverBattleUnitId) ?: return GiveItemRefusalDto.NOT_STORED
        inventory.giveItemRefusal(lootableItemId)?.let { refusal -> return refusal }
        return when {
            otherMemberIds.isEmpty() -> GiveItemRefusalDto.NO_ONE
            searchItemReceivers(giverBattleUnitId).isEmpty() -> GiveItemRefusalDto.INVENTORIES_FULL
            else -> null
        }
    }
}
