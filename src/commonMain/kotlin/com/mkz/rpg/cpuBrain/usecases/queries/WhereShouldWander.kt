package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBrain.usecases.services.REST_CHANCE
import com.mkz.rpg.cpuBrain.usecases.services.WANDER_DISTANCE
import com.mkz.rpg.cpuBrain.usecases.services.terrainHealthContribution
import kotlin.random.Random

/**
 * Where a CPU battle unit out of a battle steps to while it wanders around its home (the tile it was deployed on), or
 * `null` when it rests. It wanders within its territory: the tiles it can walk to from home in [WANDER_DISTANCE] tiles
 * or fewer, without stepping on harmful terrain, through the battle units ([SearchDistancesFromHome]).
 *
 * Within its territory, it rests at random ([REST_CHANCE]), but on harmful terrain (e.g.: lava). Otherwise, it scores
 * the free tiles next to it that are not harmful, within its territory or nearer to home, and steps to one of them at
 * random, weighted by their utility:
 * - Roam: how much further from home the tile is, weighted by how near home the battle unit is. Near home, it walks away.
 * - Home: how much nearer to home the tile is, weighted by how far from home the battle unit is. Far from home, it walks back.
 * - Terrain: the health effect of the tile terrain, weighted by its healing need, as when moving in a battle.
 *
 * Outside its territory (e.g.: after chasing the party in a battle), and while [isReturning] home after leaving it, it
 * heads home: it never rests, and only steps nearer to home, around the harmful terrain. Once home, it wanders again. It
 * stays when home can not be walked to.
 *
 * Is returning: whether it left its territory and has not been back home since, as the wandering remembers.
 */
class WhereShouldWander(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val searchDistancesFromHome: SearchDistancesFromHome,
    private val healingNeed: HealingNeed,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
    private val random: Random = Random.Default,
) {
    operator fun invoke(
        battleUnitId: String,
        homeRow: Int,
        homeColumn: Int,
        isReturning: Boolean = false,
    ): PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return null
        val position = searchPosition(battleUnitId = battleUnitId) ?: return null
        val distancesFromHome = searchDistancesFromHome(homeRow = homeRow, homeColumn = homeColumn)
        val distanceFromHome = distancesFromHome[position] ?: return null
        val isHeadingHome = distanceFromHome > WANDER_DISTANCE || (isReturning && distanceFromHome > 0)
        val isOnHarmfulTerrain = terrainHealthEffect(position) < 0
        if (!isHeadingHome && !isOnHarmfulTerrain && random.nextDouble() < REST_CHANCE) return null
        // The free tiles next to it that are not harmful, with their distance from home: nearer to home when it heads
        // home, and within its territory otherwise.
        val steps =
            adjacentPositions(position)
                .filter { step -> canBattlefieldTileBeOccupied(row = step.row, column = step.column) }
                .filter { step -> terrainHealthEffect(step) >= 0 }
                .mapNotNull { step -> distancesFromHome[step]?.let { distance -> step to distance } }
                .filter { (_, distance) -> if (isHeadingHome) distance < distanceFromHome else distance <= WANDER_DISTANCE }
        if (steps.isEmpty()) return null
        // The distances are normalised between the steps and the current tile, so staying is the reference.
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
                        maxHealth = battleUnit.maximumHealthPoints,
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

    // Picks a tile at random, each one as likely as its utility. Without any utility, the battle unit stays.
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

    // In the order the tiles are scored: north, east, south and west.
    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row, position.column + 1),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
        )
}
