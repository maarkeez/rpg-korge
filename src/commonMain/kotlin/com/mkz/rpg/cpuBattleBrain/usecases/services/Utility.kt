package com.mkz.rpg.cpuBattleBrain.usecases.services

/** Minimum utility the best option must gain over doing nothing (e.g.: staying still, not casting) for the CPU to act. */
const val MINIMUM_UTILITY_GAIN = 0.05

/** How many tiles, at most, a CPU battle unit walks to an enemy battle unit to engage it: approach it and cast at it. */
const val ENGAGEMENT_DISTANCE = 5

/** How many tiles, at most, a CPU battle unit out of a battle wanders from its home: the tile it was deployed on. */
const val WANDER_DISTANCE = 3

/** How likely a CPU battle unit wandering within its territory rests instead of taking a step. */
const val REST_CHANCE = 0.5

/**
 * How much the health effect of a terrain is worth to a battle unit: the more it needs healing, the more it is drawn to
 * healing terrain (e.g.: water) and kept away from harmful terrain (e.g.: lava).
 */
fun terrainHealthContribution(
    terrainHealthEffect: Int,
    healingNeed: Double,
    maxHealth: Int,
): Double {
    if (terrainHealthEffect == 0 || maxHealth <= 0) return 0.0
    return healingNeed * terrainHealthEffect / maxHealth.toDouble()
}
