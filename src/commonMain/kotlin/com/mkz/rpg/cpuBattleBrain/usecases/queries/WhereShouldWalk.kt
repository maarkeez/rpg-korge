package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanWalk
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.MINIMUM_UTILITY_GAIN
import com.mkz.rpg.cpuBattleBrain.usecases.services.terrainHealthContribution
import kotlin.math.abs

class WhereShouldWalk(
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanWalk: WhereCanWalk,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val healingNeed: HealingNeed,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
) {
    operator fun invoke(battleUnitId: String): Battlefield.Dto.PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId)!!
        val currentPosition = searchPosition(occupantId = battleUnitId)!!
        val optionTiles =
            buildList {
                addAll(whereCanWalk(battleUnitId = battleUnit.id))
                add(currentPosition)
            }
        val enemyPreference = battleUnit.remainingHealth.toDouble() / battleUnit.maximumHealth.toDouble()
        val allyPreference = 1 - enemyPreference

        val enemyBattleUnits = searchEnemiesWithinReach(battleUnitId = battleUnit.id)
        val enemyBattleUnitsPositions =
            enemyBattleUnits.associate { battleUnit ->
                battleUnit.id to searchPosition(occupantId = battleUnit.id)
            }
        val allyBattleUnits = searchBattleUnitsByPlayerId(playerId = battleUnit.playerId).filter { ally -> ally.id != battleUnit.id }
        val allyBattleUnitsPositions =
            allyBattleUnits.associate { battleUnit ->
                battleUnit.id to searchPosition(occupantId = battleUnit.id)
            }
        val utilityData = mutableMapOf<Battlefield.Dto.PositionDto, UtilityEvaluationData>()
        optionTiles.forEach { position ->
            utilityData[position] = UtilityEvaluationData()
        }
        optionTiles.forEach { position ->
            val hasDeployedEnemies = enemyBattleUnitsPositions.any { (_, position) -> position != null }
            if (!hasDeployedEnemies) {
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance = null)
            } else {
                val nearestEnemyDistance =
                    enemyBattleUnitsPositions
                        .filter { (_, enemyPosition) -> enemyPosition != null }
                        .map { (_, enemyPosition) -> tilesApart(position, enemyPosition!!) }
                        .min()
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance = nearestEnemyDistance)
            }
        }
        optionTiles.forEach { position ->
            val hasDeployedAllies = allyBattleUnitsPositions.any { (_, position) -> position != null }
            if (!hasDeployedAllies) {
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = null)
            } else {
                val nearestAllyDistance =
                    allyBattleUnitsPositions
                        .filter { (_, allyPosition) -> allyPosition != null }
                        .map { (_, allyPosition) -> tilesApart(position, allyPosition!!) }
                        .min()
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = nearestAllyDistance)
            }
        }
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
                            nearness(distance = evaluationData.nearestEnemyDistance, nearest = minimumEnemyDistance, farthest = maximumEnemyDistance),
                    )
            }
        }
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
                            nearness(distance = evaluationData.nearestAllyDistance, nearest = minimumAllyDistance, farthest = maximumAllyDistance),
                    )
            }
        }
        optionTiles.forEach { position ->
            utilityData[position] =
                utilityData[position]!!.copy(
                    terrainHealthEffect = searchTerrainHealthEffect(row = position.row, column = position.column),
                )
        }
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
                    maxHealth = battleUnit.maximumHealth,
                )
            utilityData[position] =
                evaluationData.copy(
                    enemyContribution = enemyContribution,
                    allyContribution = allyContribution,
                    terrainContribution = terrainContribution,
                    optionUtility = enemyContribution + allyContribution + terrainContribution,
                )
        }
        val (bestPosition, bestEvaluationData) = utilityData.entries.maxBy { (_, evaluationData) -> evaluationData.optionUtility!! }
        val currentPositionUtility = utilityData[currentPosition]!!.optionUtility!!
        if (abs(bestEvaluationData.optionUtility!! - currentPositionUtility) <= MINIMUM_UTILITY_GAIN) {
            return null
        }
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
        val optionUtility: Double? = null,
    )

    private fun nearness(
        distance: Int,
        nearest: Int,
        farthest: Int,
    ): Double = if (farthest == nearest) 0.0 else (farthest - distance).toDouble() / (farthest - nearest).toDouble()

    private fun tilesApart(
        from: Battlefield.Dto.PositionDto,
        to: Battlefield.Dto.PositionDto,
    ): Int = (abs(from.row - to.row) + abs(from.column - to.column))
}
