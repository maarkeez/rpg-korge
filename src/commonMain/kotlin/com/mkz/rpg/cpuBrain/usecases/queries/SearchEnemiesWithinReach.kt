package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.rpg.cpuBrain.usecases.services.ENGAGEMENT_DISTANCE
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer

/**
 * The enemy battle units a CPU battle unit engages: the ones it can walk to in [ENGAGEMENT_DISTANCE] tiles or fewer,
 * counting their tile, stepping only on tiles it could stand on. A summon reaches every enemy it can walk to, however
 * far, as it lasts only some turns, and spends them attacking.
 */
class SearchEnemiesWithinReach(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchPosition: SearchPosition,
    private val searchWalkingDistance: SearchWalkingDistance,
) {
    operator fun invoke(battleUnitId: String): List<BattleUnit.Dto> {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return emptyList()
        val position = searchPosition(battleUnitId = battleUnitId) ?: return emptyList()
        val enemyPlayer = searchEnemyPlayer(playerId = battleUnit.playerId) ?: return emptyList()
        val maximumDistance = if (battleUnit.remainingTurns != null) Int.MAX_VALUE else ENGAGEMENT_DISTANCE
        return searchBattleUnitsByPlayerId(playerId = enemyPlayer.id)
            .filter { enemy -> enemy.remainingHealthPoints > 0 }
            .filter { enemy ->
                val enemyPosition = searchPosition(battleUnitId = enemy.id) ?: return@filter false
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
