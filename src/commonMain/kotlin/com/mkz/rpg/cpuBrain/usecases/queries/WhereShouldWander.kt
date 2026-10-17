package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.rpg.cpuBrain.usecases.services.REST_CHANCE
import com.mkz.rpg.cpuBrain.usecases.services.WANDER_DISTANCE
import com.mkz.rpg.cpuBrain.usecases.services.terrainHealthContribution
import kotlin.random.Random

/**
 * Where a CPU battle unit out of a battle steps to while it wanders around its home (the tile it was deployed on), or
 * `null` when it rests. It wanders within its territory: the tiles walkable from home in [WANDER_DISTANCE] tiles or
 * fewer, through the battle units, as only the terrain bounds it.
 *
 * Within its territory, it rests at random ([REST_CHANCE]), but on harmful terrain (e.g.: lava). Otherwise, it scores
 * the free tiles next to it that are not harmful, and steps to one of them at random, weighted by their utility:
 * - Roam: how much further from home the tile is, weighted by how near home the battle unit is. Near home, it walks away.
 * - Home: how much nearer to home the tile is, weighted by how far from home the battle unit is. Far from home, it walks back.
 * - Terrain: the health effect of the tile terrain, weighted by its healing need, as when moving in a battle.
 *
 * Outside its territory (e.g.: after a battle), it stays where it is.
 */
class WhereShouldWander(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPosition: SearchPosition,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val searchWalkingDistance: SearchWalkingDistance,
    private val healingNeed: HealingNeed,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
    private val random: Random = Random.Default,
) {
    operator fun invoke(
        battleUnitId: String,
        homeRow: Int,
        homeColumn: Int,
    ): PositionDto? {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return null
        val position = searchPosition(battleUnitId = battleUnitId) ?: return null
        val home = PositionDto(row = homeRow, column = homeColumn)
        val distanceFromHome = distanceFromHome(home, position) ?: return null
        val isOnHarmfulTerrain = terrainHealthEffect(position) < 0
        if (!isOnHarmfulTerrain && random.nextDouble() < REST_CHANCE) return null
        // The free tiles next to it, within its territory and not harmful, with their distance from home.
        val steps =
            adjacentPositions(position)
                .filter { step -> canBattlefieldTileBeOccupied(row = step.row, column = step.column) }
                .filter { step -> terrainHealthEffect(step) >= 0 }
                .mapNotNull { step -> distanceFromHome(home, step)?.let { distance -> step to distance } }
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

    private fun distanceFromHome(
        home: PositionDto,
        position: PositionDto,
    ): Int? =
        searchWalkingDistance(
            fromRow = home.row,
            fromColumn = home.column,
            toRow = position.row,
            toColumn = position.column,
            maximumDistance = WANDER_DISTANCE,
            throughBattleUnits = true,
        )

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
