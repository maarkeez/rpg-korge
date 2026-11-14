package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.Inventory.Dto.GiveGoldRefusalDto
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.GiveRule

class SearchGiveGoldRefusal(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
    private val searchGoldReceivers: SearchGoldReceivers,
) {
    operator fun invoke(giverBattleUnitId: String): GiveGoldRefusalDto? {
        val otherMemberIds = giveRule.otherMemberIds(giverBattleUnitId) ?: return GiveGoldRefusalDto.NOT_EXPLORING
        val gold = inventoryRepository.searchByBattleUnitId(giverBattleUnitId)?.toDto()?.gold ?: 0
        return when {
            gold == 0 -> GiveGoldRefusalDto.NO_GOLD
            otherMemberIds.isEmpty() -> GiveGoldRefusalDto.NO_ONE
            searchGoldReceivers(giverBattleUnitId).isEmpty() -> GiveGoldRefusalDto.GOLD_FULL
            else -> null
        }
    }
}
