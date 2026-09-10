package com.mkz.rpg.battle.domain

sealed class BattleError(
    message: String,
) : Throwable(message = message) {
    class MinimumTwoPlayersRequired : BattleError("At least two players required")
}
