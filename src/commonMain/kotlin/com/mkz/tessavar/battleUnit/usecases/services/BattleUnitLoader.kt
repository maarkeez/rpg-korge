package com.mkz.tessavar.battleUnit.usecases.services

interface BattleUnitLoader {
    suspend fun initResources() {}

    fun loadDeployments(): List<Deployment>

    data class Deployment(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    )
}
