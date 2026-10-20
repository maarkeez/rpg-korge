package com.mkz.rpg.cpuWanderBrain.domain

sealed class CpuWanderBrainError(
    message: String,
) : Throwable(message = message) {
    class NotTheNextBattleUnit : CpuWanderBrainError("Only the next battle unit to wander can rest or step")

    class AlreadyAwaitingAStep : CpuWanderBrainError("A battle unit can not wander until the awaited step has been walked")
}
