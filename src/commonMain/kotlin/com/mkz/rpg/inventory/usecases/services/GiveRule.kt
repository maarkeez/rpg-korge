package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotExploringTheParty
import com.mkz.rpg.party.usecases.queries.SearchParty

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

    fun otherMemberIds(giverBattleUnitId: String): List<String>? {
        val party = searchParty()?.takeIf { party -> party.isExploring && giverBattleUnitId in party.battleUnitIds } ?: return null
        return party.battleUnitIds - giverBattleUnitId
    }
}
