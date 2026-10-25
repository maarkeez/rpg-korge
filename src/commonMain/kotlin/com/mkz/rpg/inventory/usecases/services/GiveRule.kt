package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotExploringTheParty
import com.mkz.rpg.party.usecases.queries.SearchParty

/**
 * A battle unit can only give an item to another member of its party, while the party explores: not in a battle, and
 * not while talking to an npc. The distance between them does not matter.
 */
class GiveRule(
    private val searchParty: SearchParty,
) {
    fun requireGiving(
        giverBattleUnitId: String,
        receiverBattleUnitId: String,
    ) {
        val otherMemberIds = otherMemberIds(giverBattleUnitId) ?: throw NotExploringTheParty()
        if (receiverBattleUnitId !in otherMemberIds) throw NotAPartyMember()
    }

    /** The members of the exploring party but the giver, in party order. `null` when the giver can not give, e.g.: in a battle. */
    fun otherMemberIds(giverBattleUnitId: String): List<String>? {
        val party = searchParty()?.takeIf { party -> party.isExploring && giverBattleUnitId in party.battleUnitIds } ?: return null
        return party.battleUnitIds - giverBattleUnitId
    }
}
