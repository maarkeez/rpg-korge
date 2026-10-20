package com.mkz.rpg.cpuBattleBrain.domain

sealed class CpuBattleBrainError(
    message: String,
) : Throwable(message = message) {
    class NoBattleUnitsToManage : CpuBattleBrainError("At least one battle unit is required to play a CPU turn")

    class NotTheNextBattleUnit : CpuBattleBrainError("Only the next battle unit to manage can act")

    class AlreadyAwaitingAnAction : CpuBattleBrainError("A new action can not be requested while awaiting the previous one")
}
