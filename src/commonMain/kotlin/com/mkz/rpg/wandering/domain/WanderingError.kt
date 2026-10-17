package com.mkz.rpg.wandering.domain

sealed class WanderingError(
    message: String,
) : Throwable(message = message) {
    class NotTheNextBattleUnit : WanderingError("Only the next battle unit to wander can rest or step")

    class AlreadyAwaitingAStep : WanderingError("A battle unit can not wander until the awaited step has been walked")
}
