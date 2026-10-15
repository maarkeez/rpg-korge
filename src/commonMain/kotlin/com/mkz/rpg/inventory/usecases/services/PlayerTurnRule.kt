package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn

/** The inventory can only be used during the turn of its battle unit player, although it does not cost any turn action. */
class PlayerTurnRule(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchBattle: SearchBattle,
) {
    fun requirePlayerTurn(battleUnitId: String) {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: throw InventoryBattleUnitDoesNotExist()
        if (searchBattle()?.currentPlayerTurn != battleUnit.playerId) throw NotTheBattleUnitPlayerTurn()
    }
}
