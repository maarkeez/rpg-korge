package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveGoldRefusalDto
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

/**
 * Why the battle unit can not give gold to any other party member: the party is not exploring, it carries no gold, it is
 * alone in the party, or every other member carries the most gold it can. `null` when it can give gold to someone.
 */
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
