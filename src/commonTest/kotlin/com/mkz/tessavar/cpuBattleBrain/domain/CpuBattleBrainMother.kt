package com.mkz.tessavar.cpuBattleBrain.domain

object CpuBattleBrainMother {
    fun cpuBattleBrain(
        playerId: String = "cpu",
        battleUnitIds: List<String> = listOf("unit-1", "unit-2"),
    ): CpuBattleBrain = CpuBattleBrain.startPlayerTurn(playerId = playerId, battleUnitIds = battleUnitIds).pullEvents().second
}
