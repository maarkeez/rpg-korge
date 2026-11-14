package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotExploringTheParty
import com.mkz.tessavar.party.usecases.queries.SearchParty

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
