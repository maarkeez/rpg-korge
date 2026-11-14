package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.tessavar.cpuBattleBrain.usecases.services.REACH
import com.mkz.tessavar.player.usecases.queries.SearchOpponent

class SearchEnemiesWithinReach(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchOpponent: SearchOpponent,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchPosition: SearchPosition,
    private val searchWalkingDistance: SearchWalkingDistance,
) {
    operator fun invoke(battleUnitId: String): List<BattleUnit.Dto> {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return emptyList()
        val position = searchPosition(occupantId = battleUnitId) ?: return emptyList()
        val opponent = searchOpponent(playerId = battleUnit.playerId) ?: return emptyList()
        val maximumDistance = if (battleUnit.remainingSummonDuration != null) Int.MAX_VALUE else REACH
        return searchBattleUnitsByPlayerId(playerId = opponent.id)
            .filter { enemy -> enemy.remainingHealth > 0 }
            .filter { enemy ->
                val enemyPosition = searchPosition(occupantId = enemy.id) ?: return@filter false
                searchWalkingDistance(
                    fromRow = position.row,
                    fromColumn = position.column,
                    toRow = enemyPosition.row,
                    toColumn = enemyPosition.column,
                    maximumDistance = maximumDistance,
                ) != null
            }
    }
}
