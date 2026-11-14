package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.tessavar.party.usecases.queries.SearchParty

class PlayerTurnRule(
    private val searchParty: SearchParty,
    private val searchBattle: SearchBattle,
) {
    fun requirePlayerTurn(battleUnitId: String) {
        val party = searchParty()?.takeIf { party -> battleUnitId in party.battleUnitIds } ?: throw NotAPartyMember()
        val battle = searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return
        if (battle.currentPlayerId != party.playerId) throw NotTheBattleUnitPlayerTurn()
    }
}
