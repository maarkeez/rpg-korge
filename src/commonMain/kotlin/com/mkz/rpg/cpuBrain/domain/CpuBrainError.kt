package com.mkz.rpg.cpuBrain.domain

sealed class CpuBrainError(
    message: String,
) : Throwable(message = message) {
    class NoBattleUnitsToManage : CpuBrainError("At least one battle unit is required to play a CPU turn")

    class NotTheNextBattleUnit : CpuBrainError("Only the next battle unit to manage can act")

    class AlreadyAwaitingAnAction : CpuBrainError("A new action can not be requested while awaiting the previous one")
}
