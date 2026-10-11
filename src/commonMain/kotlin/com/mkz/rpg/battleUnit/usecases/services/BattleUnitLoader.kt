package com.mkz.rpg.battleUnit.usecases.services

interface BattleUnitLoader {
    suspend fun initResources() {}

    /** The battle units deployed on the battlefield when the battle starts. */
    fun loadDeployments(): List<Deployment>

    data class Deployment(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    )
}
