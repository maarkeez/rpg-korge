package com.mkz.rpg.battle.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattleEvent : DomainEvent {
    object BattleStarted : BattleEvent

    object RequestFinishPlayerTurn : BattleEvent

    data class RequestStartBattle(
        val engagingPlayerId: String,
        val engagedPlayerId: String,
        val engagingBattleUnitIds: List<String>,
        val engagedBattleUnitIds: List<String>,
    ) : BattleEvent

    data class RequestStartNextRound(
        val joiningPlayerId: String?,
        val joiningBattleUnitIds: List<String>,
        val leavingBattleUnitIds: List<String>,
    ) : BattleEvent

    data class RequestJoinBattle(
        val battleUnitId: String,
        val playerId: String,
    ) : BattleEvent

    data class RequestLeaveBattle(
        val battleUnitId: String,
    ) : BattleEvent

    data class RequestDefeatPlayer(
        val playerId: String,
    ) : BattleEvent

    data class RequestResolveDisengagedBattle(
        val joiningPlayerId: String?,
        val joiningBattleUnitIds: List<String>,
    ) : BattleEvent

    data class BattleDisengaged(
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    object BattleFinishedWithoutWinner : BattleEvent

    data class RoundStarted(
        val round: Int,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    data class RoundFinished(
        val round: Int,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

    data class PlayerTurnFinished(
        val playerId: String,
        val battleUnitIds: Set<String>,
    ) : BattleEvent

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
