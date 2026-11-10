package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.IsTileVacant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.REST_CHANCE
import com.mkz.rpg.cpuBattleBrain.usecases.services.WANDER_DISTANCE
import com.mkz.rpg.cpuBattleBrain.usecases.services.terrainHealthContribution
import kotlin.random.Random

class WhereShouldWander(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val isTileVacant: IsTileVacant,
    private val healingNeed: HealingNeed,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
    private val random: Random = Random.Default,
) {
    operator fun invoke(
        battleUnitId: String,
        distancesFromHome: Map<PositionDto, Int>,
        isReturning: Boolean = false,
    ): PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return null
        val position = searchPosition(occupantId = battleUnitId) ?: return null
        val distanceFromHome = distancesFromHome[position] ?: return null
        val returns = distanceFromHome > WANDER_DISTANCE || (isReturning && distanceFromHome > 0)
        val isOnHarmfulTerrain = terrainHealthEffect(position) < 0
        if (!returns && !isOnHarmfulTerrain && random.nextDouble() < REST_CHANCE) return null
        val steps =
            northEastSouthWestPositions(position)
                .filter { step -> isTileVacant(row = step.row, column = step.column) }
                .filter { step -> terrainHealthEffect(step) >= 0 }
                .mapNotNull { step -> distancesFromHome[step]?.let { distance -> step to distance } }
                .filter { (_, distance) -> if (returns) distance < distanceFromHome else distance <= WANDER_DISTANCE }
        if (steps.isEmpty()) return null
        val distances = steps.map { (_, distance) -> distance } + distanceFromHome
        val nearestDistance = distances.min()
        val furthestDistance = distances.max()
        val homePreference = (distanceFromHome.toDouble() / WANDER_DISTANCE).coerceAtMost(1.0)
        val roamPreference = 1 - homePreference
        val currentHealingNeed = healingNeed(battleUnitId = battleUnitId)
        val utilities =
            steps.map { (step, distance) ->
                val furtherFromHome = normalise(distance - nearestDistance, furthestDistance - nearestDistance)
                val nearerToHome = normalise(furthestDistance - distance, furthestDistance - nearestDistance)
                val terrainContribution =
                    terrainHealthContribution(
                        terrainHealthEffect = terrainHealthEffect(step),
                        healingNeed = currentHealingNeed,
                        maxHealth = battleUnit.maximumHealth,
                    )
                step to (roamPreference * furtherFromHome + homePreference * nearerToHome + terrainContribution).coerceAtLeast(0.0)
            }
        return pickWeighted(utilities)
    }

    private fun terrainHealthEffect(position: PositionDto): Int = searchTerrainHealthEffect(row = position.row, column = position.column)

    private fun normalise(
        value: Int,
        range: Int,
    ): Double = if (range == 0) 0.0 else value.toDouble() / range.toDouble()

    private fun pickWeighted(utilities: List<Pair<PositionDto, Double>>): PositionDto? {
        val totalUtility = utilities.sumOf { (_, utility) -> utility }
        if (totalUtility <= 0.0) return null
        val roll = random.nextDouble() * totalUtility
        var accumulatedUtility = 0.0
        utilities.forEach { (step, utility) ->
            accumulatedUtility += utility
            if (roll < accumulatedUtility) return step
        }
        return utilities.last { (_, utility) -> utility > 0.0 }.first
    }

    private fun northEastSouthWestPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row, position.column + 1),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
        )
}
