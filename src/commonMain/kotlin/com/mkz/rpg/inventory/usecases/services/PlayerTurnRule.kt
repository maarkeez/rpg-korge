package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn

/**
 * In a battle, the inventory can only be used during the turn of its battle unit player, although it does not cost any
 * turn action. Out of the battles (e.g.: while exploring), there are no turns, so it can always be used.
 */
class PlayerTurnRule(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchBattle: SearchBattle,
) {
    fun requirePlayerTurn(battleUnitId: String) {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: throw InventoryBattleUnitDoesNotExist()
        val battle = searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return
        if (battle.currentPlayerTurn != battleUnit.playerId) throw NotTheBattleUnitPlayerTurn()
    }
}
