package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.MINIMUM_UTILITY_GAIN
import com.mkz.rpg.cpuBattleBrain.usecases.services.terrainHealthContribution
import kotlin.math.abs

class WhereShouldMove(
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanMove: WhereCanMove,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val healingNeed: HealingNeed,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
) {
    operator fun invoke(battleUnitId: String): Battlefield.Dto.PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId)!!
        val currentPosition = searchPosition(battleUnitId = battleUnitId)!!
        val candidatePositions =
            buildList {
                addAll(whereCanMove(battleUnitId = battleUnit.id))
                add(currentPosition)
            }
        val enemyPreference = battleUnit.remainingHealthPoints.toDouble() / battleUnit.maximumHealthPoints.toDouble()
        val allyPreference = 1 - enemyPreference

        // Only the enemies within reach attract the battle unit: the others are ignored.
        val enemyBattleUnits = searchEnemiesWithinReach(battleUnitId = battleUnit.id)
        val enemyBattleUnitsPositions =
            enemyBattleUnits.associate { battleUnit ->
                battleUnit.id to searchPosition(battleUnitId = battleUnit.id)
            }
        val allyBattleUnits = searchBattleUnitsByPlayerId(playerId = battleUnit.playerId).filter { ally -> ally.id != battleUnit.id }
        val allyBattleUnitsPositions =
            allyBattleUnits.associate { battleUnit ->
                battleUnit.id to searchPosition(battleUnitId = battleUnit.id)
            }
        // Initialize
        val utilityData = mutableMapOf<Battlefield.Dto.PositionDto, UtilityEvaluationData>()
        candidatePositions.forEach { position ->
            utilityData[position] = UtilityEvaluationData()
        }
        // Evaluate distance from every candidate to nearest enemy
        candidatePositions.forEach { position ->
            val hasDeployedEnemies = enemyBattleUnitsPositions.any { (_, position) -> position != null }
            if (!hasDeployedEnemies) {
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance = null)
            } else {
                val nearestEnemyDistance =
                    enemyBattleUnitsPositions
                        .filter { (_, enemyPosition) -> enemyPosition != null }
                        .map { (_, enemyPosition) -> manhattanDistance(position, enemyPosition!!) }
                        .min()
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance = nearestEnemyDistance)
            }
        }
        // Evaluate distance from every candidate to nearest ally
        candidatePositions.forEach { position ->
            val hasDeployedAllies = allyBattleUnitsPositions.any { (_, position) -> position != null }
            if (!hasDeployedAllies) {
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = null)
            } else {
                val nearestAllyDistance =
                    allyBattleUnitsPositions
                        .filter { (_, allyPosition) -> allyPosition != null }
                        .map { (_, allyPosition) -> manhattanDistance(position, allyPosition!!) }
                        .min()
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = nearestAllyDistance)
            }
        }
        // Normalize enemy distances
        val minimumEnemyDistance =
            utilityData.entries
                .filter { (_, evaluationData) -> evaluationData.nearestEnemyDistance != null }
                .minOfOrNull { (_, evaluationData) -> evaluationData.nearestEnemyDistance!! }
        val maximumEnemyDistance =
            utilityData.entries
                .filter { (_, evaluationData) -> evaluationData.nearestEnemyDistance != null }
                .maxOfOrNull { (_, evaluationData) -> evaluationData.nearestEnemyDistance!! }
        utilityData.entries.forEach { (position, evaluationData) ->
            if (maximumEnemyDistance != null && minimumEnemyDistance != null && evaluationData.nearestEnemyDistance != null) {
                utilityData[position] =
                    evaluationData.copy(
                        nearestEnemyDistanceNormalized =
                            (maximumEnemyDistance.toDouble() - evaluationData.nearestEnemyDistance.toDouble()) / (maximumEnemyDistance.toDouble() - minimumEnemyDistance.toDouble()),
                    )
            }
        }
        // Normalize ally distances
        val minimumAllyDistance =
            utilityData.entries
                .filter { (_, evaluationData) -> evaluationData.nearestAllyDistance != null }
                .minOfOrNull { (_, evaluationData) -> evaluationData.nearestAllyDistance!! }
        val maximumAllyDistance =
            utilityData.entries
                .filter { (_, evaluationData) -> evaluationData.nearestAllyDistance != null }
                .maxOfOrNull { (_, evaluationData) -> evaluationData.nearestAllyDistance!! }
        utilityData.entries.forEach { (position, evaluationData) ->
            if (maximumAllyDistance != null && minimumAllyDistance != null && evaluationData.nearestAllyDistance != null) {
                utilityData[position] =
                    evaluationData.copy(
                        nearestAllyDistanceNormalized =
                            (maximumAllyDistance.toDouble() - evaluationData.nearestAllyDistance.toDouble()) / (maximumAllyDistance.toDouble() - minimumAllyDistance.toDouble()),
                    )
            }
        }
        // Evaluate the health effect of the terrain of every candidate
        candidatePositions.forEach { position ->
            utilityData[position] =
                utilityData[position]!!.copy(
                    terrainHealthEffect = searchTerrainHealthEffect(row = position.row, column = position.column),
                )
        }
        // Calculate utility contribution
        val currentHealingNeed = healingNeed(battleUnitId = battleUnit.id)
        utilityData.entries.forEach { (position, evaluationData) ->
            val enemyContribution =
                if (evaluationData.nearestEnemyDistanceNormalized != null) {
                    enemyPreference * evaluationData.nearestEnemyDistanceNormalized
                } else {
                    0.0
                }
            val allyContribution =
                if (evaluationData.nearestAllyDistanceNormalized != null) {
                    allyPreference * evaluationData.nearestAllyDistanceNormalized
                } else {
                    0.0
                }
            val terrainContribution =
                terrainHealthContribution(
                    terrainHealthEffect = evaluationData.terrainHealthEffect ?: 0,
                    healingNeed = currentHealingNeed,
                    maxHealth = battleUnit.maximumHealthPoints,
                )
            utilityData[position] =
                evaluationData.copy(
                    enemyContribution = enemyContribution,
                    allyContribution = allyContribution,
                    terrainContribution = terrainContribution,
                    candidateUtility = enemyContribution + allyContribution + terrainContribution,
                )
        }
        // Calculate the best candidate
        val (bestPosition, bestEvaluationData) = utilityData.entries.maxBy { (_, evaluationData) -> evaluationData.candidateUtility!! }
        // Check threshold to prevent movements with minimum gain
        val currentPositionUtility = utilityData[currentPosition]!!.candidateUtility!!
        if (abs(bestEvaluationData.candidateUtility!! - currentPositionUtility) <= MINIMUM_UTILITY_GAIN) {
            return null
        }
        // Check if we are moving at all
        if (bestPosition == currentPosition) {
            return null
        }
        return bestPosition
    }

    data class UtilityEvaluationData(
        val nearestEnemyDistance: Int? = null,
        val nearestAllyDistance: Int? = null,
        val nearestEnemyDistanceNormalized: Double? = null,
        val nearestAllyDistanceNormalized: Double? = null,
        val terrainHealthEffect: Int? = null,
        val enemyContribution: Double? = null,
        val allyContribution: Double? = null,
        val terrainContribution: Double? = null,
        val candidateUtility: Double? = null,
    )

    private fun manhattanDistance(
        from: Battlefield.Dto.PositionDto,
        to: Battlefield.Dto.PositionDto,
    ): Int = (abs(from.row - to.row) + abs(from.column - to.column))
}
