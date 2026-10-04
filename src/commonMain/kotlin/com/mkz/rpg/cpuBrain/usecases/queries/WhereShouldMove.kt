package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.math.abs

class WhereShouldMove(
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanMove: WhereCanMove,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val searchUnitById: SearchUnitById,
    private val healingNeed: HealingNeed,
    private val searchBattlefield: SearchBattlefield,
    private val searchTerrainById: SearchTerrainById,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(battleUnitId: String): Battlefield.Dto.PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId)!!
        val currentPosition = searchPosition(battleUnitId = battleUnitId)!!
        val candidatePositions =
            buildList {
                addAll(whereCanMove(battleUnitId = battleUnit.id))
                add(currentPosition)
            }
        val unit = searchUnitById(id = battleUnit.unitId)!!

        val enemyPreference = battleUnit.remainingHealthPoints.toDouble() / unit.healthPoints.toDouble()
        val allyPreference = 1 - enemyPreference

        val enemyPlayer = searchEnemyPlayer(playerId = battleUnit.playerId)
        val enemyBattleUnits =
            if (enemyPlayer != null) {
                searchBattleUnitsByPlayerId(playerId = enemyPlayer.id)
            } else {
                emptyList()
            }
        val enemyBattleUnitsPositions =
            enemyBattleUnits.associate { battleUnit ->
                battleUnit.id to searchPosition(battleUnitId = battleUnit.id)
            }
        val allyBattleUnits = searchBattleUnitsByPlayerId(playerId = battleUnit.playerId)
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
        val battlefield = searchBattlefield()
        candidatePositions.forEach { position ->
            utilityData[position] =
                utilityData[position]!!.copy(
                    terrainHealthEffect = terrainHealthEffect(battlefield, position),
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
                    terrainHealthEffect = evaluationData.terrainHealthEffect,
                    healingNeed = currentHealingNeed,
                    maxHealth = unit.healthPoints,
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
        val threshold = 0.05
        val currentPositionUtility = utilityData[currentPosition]!!.candidateUtility!!
        if (abs(bestEvaluationData.candidateUtility!! - currentPositionUtility) <= threshold) {
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

    private fun terrainHealthEffect(
        battlefield: Battlefield.Dto?,
        position: Battlefield.Dto.PositionDto,
    ): Int {
        val terrainId = battlefield?.tiles?.get(position)?.terrainId ?: return 0
        val effectId = searchTerrainById(terrainId)?.effectId ?: return 0
        val effect = searchEffectById(effectId) ?: return 0
        return when (effect.outcome.type) {
            INCREASE_HEALTH -> effect.outcome.increaseHealth!!.healing
            DECREASE_HEALTH -> -effect.outcome.decreaseHealth!!.damage
            else -> 0
        }
    }

    private fun terrainHealthContribution(
        terrainHealthEffect: Int?,
        healingNeed: Double,
        maxHealth: Int,
    ): Double {
        if (terrainHealthEffect == null || terrainHealthEffect == 0 || maxHealth <= 0) return 0.0
        return healingNeed * terrainHealthEffect / maxHealth.toDouble()
    }
}
