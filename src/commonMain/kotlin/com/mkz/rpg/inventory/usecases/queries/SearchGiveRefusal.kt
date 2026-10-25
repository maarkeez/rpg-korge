package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveRefusalDto
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule

/**
 * Why the battle unit can not give the item to any other party member: the party is not exploring, it stores no copy of
 * the item, its only copy is equipped, it is alone in the party, or every other member has no space left. `null` when it
 * can give it to someone.
 */
class SearchGiveRefusal(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
    private val searchGiveReceivers: SearchGiveReceivers,
) {
    operator fun invoke(
        giverBattleUnitId: String,
        lootableItemId: String,
    ): GiveRefusalDto? {
        val otherMemberIds = giveRule.otherMemberIds(giverBattleUnitId) ?: return GiveRefusalDto.NOT_EXPLORING
        val inventory = inventoryRepository.searchByBattleUnitId(giverBattleUnitId) ?: return GiveRefusalDto.NOT_STORED
        inventory.giveRefusal(lootableItemId)?.let { refusal -> return refusal }
        return when {
            otherMemberIds.isEmpty() -> GiveRefusalDto.NO_ONE
            searchGiveReceivers(giverBattleUnitId).isEmpty() -> GiveRefusalDto.BAGS_FULL
            else -> null
        }
    }
}
