package com.mkz.rpg.battle.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattleEvent : DomainEvent {
    object BattleStarted : BattleEvent

    object RequestFinishPlayerTurn : BattleEvent

    /** The battle finished without a winner, as only one player is left with battle units in it (e.g.: the CPU battle units were defeated or left behind). */
    object BattleFinished : BattleEvent

    /** Battle units: those in the battle. */
    data class BattleRoundStarted(
        val round: Int,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    /** Battle units: those in the battle. */
    data class BattleRoundFinished(
        val round: Int,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    /** Published before the next turn starts or the round finishes. Battle units: those in the battle, of every player. */
    data class PlayerTurnFinished(
        val playerId: String,
        val battleUnitIds: Set<String>,
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
