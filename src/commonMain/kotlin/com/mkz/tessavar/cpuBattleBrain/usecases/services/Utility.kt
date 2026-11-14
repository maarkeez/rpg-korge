package com.mkz.tessavar.cpuBattleBrain.usecases.services

const val MINIMUM_UTILITY_GAIN = 0.05

const val REACH = 5

const val WANDER_DISTANCE = 3

const val REST_CHANCE = 0.5

fun terrainHealthContribution(
    terrainHealthEffect: Int,
    healingNeed: Double,
    maxHealth: Int,
): Double {
    if (terrainHealthEffect == 0 || maxHealth <= 0) return 0.0
    return healingNeed * terrainHealthEffect / maxHealth.toDouble()
}
