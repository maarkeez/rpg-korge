package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.party.usecases.queries.SearchParty

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
