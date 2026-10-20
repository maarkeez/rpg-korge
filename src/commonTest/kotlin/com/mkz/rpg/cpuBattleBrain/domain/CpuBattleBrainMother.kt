package com.mkz.rpg.cpuBattleBrain.domain

object CpuBattleBrainMother {
    fun cpuBattleBrain(
        playerId: String = "cpu",
        battleUnitIds: List<String> = listOf("unit-1", "unit-2"),
    ): CpuBattleBrain = CpuBattleBrain.startTurn(playerId = playerId, battleUnitIds = battleUnitIds).pullEvents().second
}
