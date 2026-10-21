package com.mkz.rpg.battle.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattleEvent : DomainEvent {
    object BattleStarted : BattleEvent

    object RequestFinishPlayerTurn : BattleEvent

    /**
     * Starts the first round of a battle, e.g.: when the CPU engages the exploring player.
     * Engaging battle units: those that engage. Engaged battle units: those engaged (e.g.: every living one of the engaged player).
     */
    data class RequestStartBattle(
        val engagingPlayerId: String,
        val engagedPlayerId: String,
        val engagingBattleUnitIds: List<String>,
        val engagedBattleUnitIds: List<String>,
    ) : BattleEvent

    /**
     * Starts the next round, once a round finishes, with the joining battle units and without the leaving ones.
     * Joining player: the player of the joining battle units, null when there is none.
     */
    data class RequestStartNextRound(
        val joiningPlayerId: String?,
        val joiningBattleUnitIds: List<String>,
        val leavingBattleUnitIds: List<String>,
    ) : BattleEvent

    /** Adds the battle unit, of the player, to the battle when there is one, e.g.: when it is deployed during the battle. */
    data class RequestJoinBattle(
        val battleUnitId: String,
        val playerId: String,
    ) : BattleEvent

    /** Removes the battle unit from the battle when there is one, e.g.: when it is defeated. */
    data class RequestLeaveBattle(
        val battleUnitId: String,
    ) : BattleEvent

    /** Defeats the player, e.g.: once all their battle units are defeated, in a battle or while exploring. */
    data class RequestDefeatPlayer(
        val playerId: String,
    ) : BattleEvent

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
