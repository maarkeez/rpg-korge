package com.mkz.rpg.cpuBattleBrain.domain

sealed class CpuBattleBrainError(
    message: String,
) : Throwable(message = message) {
    class NoBattleUnitsToDecideFor : CpuBattleBrainError("At least one battle unit is required to play a CPU player turn")

    class NotTheNextBattleUnit : CpuBattleBrainError("Only the next battle unit to decide for can act")

    class AlreadyAwaitingADecision : CpuBattleBrainError("A new decision can not be requested while awaiting the previous one")
}
