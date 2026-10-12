package com.mkz.rpg.cpuBrain.domain

object CpuBrainMother {
    fun cpuBrain(
        playerId: String = "cpu",
        battleUnitIds: List<String> = listOf("unit-1", "unit-2"),
    ): CpuBrain = CpuBrain.startTurn(playerId = playerId, battleUnitIds = battleUnitIds).pullEvents().second
}
