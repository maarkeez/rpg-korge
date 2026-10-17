package com.mkz.rpg.battle.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattleEvent : DomainEvent {
    object BattleStarted : BattleEvent

    object RequestInitialiseBattle : BattleEvent

    object RequestFinishPlayerTurn : BattleEvent

    /** Battle units: those in the battle. */
    data class BattleRoundStarted(
        val round: Int,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    data class BattleRoundFinished(
        val round: Int,
    ) : BattleEvent

    /** Battle units: those in the battle, of every player. */
    data class PlayerTurnStarted(
        val playerId: String,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    data class PlayerVictory(
        val playerId: String,
    ) : BattleEvent

    data class PlayerDefeated(
        val playerId: String,
    ) : BattleEvent
}
