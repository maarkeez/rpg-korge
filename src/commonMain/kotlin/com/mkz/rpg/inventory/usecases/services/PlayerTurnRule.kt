package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.party.usecases.queries.SearchParty

/**
 * The inventory can only be used by a party member. In a battle, only during the turn of the party player, although it does not cost any turn
 * action. Out of the battles (e.g.: while exploring), there are no turns, so it can always be used.
 */
class PlayerTurnRule(
    private val searchParty: SearchParty,
    private val searchBattle: SearchBattle,
) {
    fun requirePlayerTurn(battleUnitId: String) {
        val party = searchParty()?.takeIf { party -> battleUnitId in party.battleUnitIds } ?: throw NotAPartyMember()
        val battle = searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return
        if (battle.currentPlayerTurn != party.playerId) throw NotTheBattleUnitPlayerTurn()
    }
}
