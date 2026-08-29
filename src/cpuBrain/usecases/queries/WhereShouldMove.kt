package cpuBrain.usecases.queries

import battlefield.domain.*
import battlefield.usecases.queries.*
import battleunit.usecases.queries.*
import player.usecases.queries.*
import unit.usecases.queries.*
import kotlin.math.*

class WhereShouldMove(
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanMove: WhereCanMove,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val searchUnitById: SearchUnitById,
) {

    operator fun invoke(battleUnitId: String) : Battlefield.Dto.PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId)!!
        val currentPosition = searchPosition(battleUnitId = battleUnitId)!!
        val candidatePositions = buildList {
            addAll(whereCanMove(battleUnitId = battleUnit.id))
            add(currentPosition)
        }
        val unit = searchUnitById(id = battleUnit.unitId)!!

        val healthRatio = battleUnit.remainingHealthPoints / unit.healthPoints
        val enemyPreference = healthRatio
        val allyPreference = 1 - healthRatio

        val enemyPlayer = searchEnemyPlayer(playerId = battleUnit.playerId)
        val enemyBattleUnits = if(enemyPlayer != null) {
            searchBattleUnitsByPlayerId(playerId = enemyPlayer.id)
        } else {
            emptyList()
        }
        val enemyBattleUnitsPositions = enemyBattleUnits.associate { battleUnit ->
            battleUnit.id to searchPosition(battleUnitId = battleUnit.id)
        }
        val allyBattleUnits = searchBattleUnitsByPlayerId(playerId = battleUnit.playerId)
        val allyBattleUnitsPositions = allyBattleUnits.associate { battleUnit ->
            battleUnit.id to searchPosition(battleUnitId = battleUnit.id)
        }
        // Initialize
        val utilityData = mutableMapOf<Battlefield.Dto.PositionDto, UtilityEvaluationData>()
        candidatePositions.forEach { position ->
            utilityData[position] = UtilityEvaluationData()
        }
        // Evaluate distance from every candidate to nearest enemy
        candidatePositions.forEach { position ->
            val hasDeployedEnemies = enemyBattleUnitsPositions.any{ (_, position) -> position != null}
            if(!hasDeployedEnemies) {
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance= null)
            }else{
                val nearestEnemyDistance = enemyBattleUnitsPositions
                    .filter { (_, enemyPosition) -> enemyPosition != null }
                    .map { (_, enemyPosition) -> manhattanDistance(position, enemyPosition!!) }
                    .min()
                utilityData[position] = utilityData[position]!!.copy(nearestEnemyDistance = nearestEnemyDistance)
            }
        }
        // Evaluate distance from every candidate to nearest ally
        candidatePositions.forEach { position ->
            val hasDeployedAllies = allyBattleUnitsPositions.any{ (_, position) -> position != null}
            if(!hasDeployedAllies) {
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = null)
            }else{
                val nearestAllyDistance = allyBattleUnitsPositions
                    .filter { (_, allyPosition) -> allyPosition != null }
                    .map { (_, allyPosition) -> manhattanDistance(position, allyPosition!!) }
                    .min()
                utilityData[position] = utilityData[position]!!.copy(nearestAllyDistance = nearestAllyDistance)
            }
        }
        // Normalize enemy distances
        val minimumEnemyDistance = utilityData.entries
            .filter { (_, evaluationData) -> evaluationData.nearestEnemyDistance != null }
            .minOfOrNull { (_, evaluationData) -> evaluationData.nearestEnemyDistance!! }
        val maximumEnemyDistance = utilityData.entries
            .filter { (_, evaluationData) -> evaluationData.nearestEnemyDistance != null }
            .maxOfOrNull { (_, evaluationData) -> evaluationData.nearestEnemyDistance!! }
        utilityData.entries.forEach { (position, evaluationData) ->
            if(maximumEnemyDistance != null && minimumEnemyDistance != null && evaluationData.nearestEnemyDistance != null){
                utilityData[position] = evaluationData.copy(
                    nearestEnemyDistanceNormalized = (maximumEnemyDistance.toDouble() - evaluationData.nearestEnemyDistance.toDouble()) / (maximumEnemyDistance.toDouble() - minimumEnemyDistance.toDouble())
                )
            }
        }
        // Normalize ally distances
        val minimumAllyDistance = utilityData.entries
            .filter { (_, evaluationData) -> evaluationData.nearestAllyDistance != null }
            .minOfOrNull { (_, evaluationData) -> evaluationData.nearestAllyDistance!! }
        val maximumAllyDistance = utilityData.entries
            .filter { (_, evaluationData) -> evaluationData.nearestAllyDistance != null }
            .maxOfOrNull { (_, evaluationData) -> evaluationData.nearestAllyDistance!! }
        utilityData.entries.forEach { (position, evaluationData) ->
            if(maximumAllyDistance != null && minimumAllyDistance != null && evaluationData.nearestAllyDistance != null){
                utilityData[position] = evaluationData.copy(
                    nearestAllyDistanceNormalized = (maximumAllyDistance.toDouble() - evaluationData.nearestAllyDistance.toDouble()) / (maximumAllyDistance.toDouble() - minimumAllyDistance.toDouble())
                )
            }
        }
        // Calculate utility contribution
        utilityData.entries.forEach { (position, evaluationData) ->
            val enemyContribution = if (evaluationData.nearestEnemyDistanceNormalized != null) {
                enemyPreference * evaluationData.nearestEnemyDistanceNormalized
            } else {
                0.0
            }
            val allyContribution = if (evaluationData.nearestAllyDistanceNormalized != null) {
                allyPreference * evaluationData.nearestAllyDistanceNormalized
            } else {
                0.0
            }
            utilityData[position] = evaluationData.copy(
                enemyContribution = enemyContribution,
                allyContribution = allyContribution,
                candidateUtility = enemyContribution + allyContribution,
            )
        }
        // Calculate the best candidate
        val (bestPosition, bestEvaluationData) = utilityData.entries.maxBy { (_, evaluationData) -> evaluationData.candidateUtility!!}
        // Check threshold to prevent movements with minimum gain
        val threshold = 0.05
        val currentPositionUtility = utilityData[currentPosition]!!.candidateUtility!!
        if(abs(bestEvaluationData.candidateUtility!! - currentPositionUtility) <= threshold) {
            return null
        }
        // Check if we are moving at all
        if(bestPosition == currentPosition){
            return null
        }
        return bestPosition
    }

    data class UtilityEvaluationData(
        val nearestEnemyDistance: Int? = null,
        val nearestAllyDistance: Int? = null,
        val nearestEnemyDistanceNormalized: Double? = null,
        val nearestAllyDistanceNormalized: Double? = null,
        val enemyContribution: Double? = null,
        val allyContribution: Double? = null,
        val candidateUtility: Double? = null,
    )

    fun manhattanDistance(from: Battlefield.Dto.PositionDto, to: Battlefield.Dto.PositionDto): Int =
        (abs(from.row - to.row) + abs(from.column - to.column))
}
