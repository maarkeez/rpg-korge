package com.mkz.rpg.battle.domain

import com.mkz.rpg.battle.domain.BattleError.MinimumTwoPlayersRequired
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.battle.domain.BattleEvent.BattleRoundFinished
import com.mkz.rpg.battle.domain.BattleEvent.BattleRoundStarted
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerDefeated
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnFinished
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Battle private constructor(
    private val turnQueue: TurnQueue,
    private val battleUnits: BattleUnits,
    private val currentRound: CurrentRound,
    private val currentPlayerTurn: CurrentPlayerTurn,
    private val isFinished: IsFinished,
    private val events: Set<BattleEvent>,
) {
    companion object {
        /**
         * Players: in the order they play each round. Battle units: those in the battle, which the turns and rounds
         * affect, each with the id of its player.
         */
        fun startFirstRound(
            players: List<String>,
            battleUnits: Map<String, String>,
        ): Battle {
            if (players.size < 2) throw MinimumTwoPlayersRequired()
            val battleUnitIds = battleUnits.keys
            return Battle(
                turnQueue = TurnQueue(players),
                battleUnits = BattleUnits(battleUnits),
                currentRound = CurrentRound(1),
                currentPlayerTurn = CurrentPlayerTurn(players.first()),
                isFinished = IsFinished(false),
                events = setOf(BattleStarted, PlayerTurnStarted(players.first(), battleUnitIds)),
            )
        }

        /**
         * Defeats the player out of the battles, e.g.: when their last battle units are defeated by the terrain while
         * exploring. Only the winner is left in it, so it finishes as a battle with a single player left.
         */
        fun defeatWhileExploring(
            defeatedPlayerId: String,
            winnerPlayerId: String,
        ): Battle =
            Battle(
                turnQueue = TurnQueue(listOf(winnerPlayerId)),
                battleUnits = BattleUnits(emptyMap()),
                currentRound = CurrentRound(0),
                currentPlayerTurn = CurrentPlayerTurn(winnerPlayerId),
                isFinished = IsFinished(false),
                events = setOf(PlayerDefeated(defeatedPlayerId)),
            )
    }

    fun toDto() =
        Dto(
            currentPlayerTurn = currentPlayerTurn.value,
            currentRound = currentRound.value,
            isFinished = isFinished.value,
            battleUnitIds = battleUnits.value.keys,
        )

    fun startNextRound(): Battle {
        val nextRound = CurrentRound(currentRound.value + 1)
        val nextPlayerTurn = CurrentPlayerTurn(turnQueue.value.first())
        return copy(
            currentRound = nextRound,
            currentPlayerTurn = CurrentPlayerTurn(turnQueue.value.first()),
            events =
                events +
                    setOf(
                        BattleRoundStarted(nextRound.value, battleUnits.value.keys),
                        PlayerTurnStarted(nextPlayerTurn.value, battleUnits.value.keys),
                    ),
        )
    }

    fun finishPlayerTurn(): Battle {
        val currentPlayerTurnIndex = turnQueue.value.indexOf(currentPlayerTurn.value)
        val nextPlayerTurnIndex = currentPlayerTurnIndex + 1
        val roundFinished = nextPlayerTurnIndex >= turnQueue.value.size
        val turnFinished = PlayerTurnFinished(currentPlayerTurn.value, battleUnits.value.keys)
        if (roundFinished) {
            return copy(
                events = events + setOf(turnFinished, BattleRoundFinished(currentRound.value, battleUnits.value.keys)),
            )
        } else {
            val nextPlayerTurn = turnQueue.value[nextPlayerTurnIndex]
            return copy(
                currentPlayerTurn = CurrentPlayerTurn(nextPlayerTurn),
                events = events + setOf(turnFinished, PlayerTurnStarted(nextPlayerTurn, battleUnits.value.keys)),
            )
        }
    }

    /** Adds the battle unit to the battle, e.g.: when it is deployed during the battle. */
    fun join(
        battleUnitId: String,
        playerId: String,
    ): Battle = copy(battleUnits = BattleUnits(battleUnits.value + (battleUnitId to playerId)))

    /** Removes the battle unit from the battle, e.g.: when it is defeated. */
    fun leave(battleUnitId: String): Battle = copy(battleUnits = BattleUnits(battleUnits.value - battleUnitId))

    fun isBattleFinished(): Boolean = turnQueue.value.size == 1

    /** It is disengaged when fewer than two of its players are left with battle units in it. */
    fun isDisengaged(): Boolean {
        val playersInBattle = battleUnits.value.values.toSet()
        return turnQueue.value.count { playerId -> playerId in playersInBattle } < 2
    }

    /** Finishes the battle without a winner, e.g.: when it is disengaged. */
    fun finishWithoutWinner(): Battle =
        copy(
            isFinished = IsFinished(true),
            events = events + BattleFinished,
        )

    fun finishBattle(): Battle =
        copy(
            isFinished = IsFinished(true),
            events = events + PlayerVictory(turnQueue.value.single()),
        )

    fun defeatPlayer(playerId: String): Battle {
        val newTurnQueue =
            buildList {
                addAll(turnQueue.value)
                remove(playerId)
            }
        val battleWithPlayerTurnUpdated: Battle = if (currentPlayerTurn.value == playerId) finishPlayerTurn() else this
        return battleWithPlayerTurnUpdated.copy(
            turnQueue = TurnQueue(newTurnQueue),
            events = events + setOf(PlayerDefeated(playerId)),
        )
    }

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class TurnQueue(
        val value: List<String>,
    )

    /** Battle unit ids, each with the id of its player. */
    @JvmInline private value class BattleUnits(
        val value: Map<String, String>,
    )

    @JvmInline private value class CurrentRound(
        val value: Int,
    )

    @JvmInline private value class CurrentPlayerTurn(
        val value: String,
    )

    @JvmInline private value class IsFinished(
        val value: Boolean,
    )

    data class Dto(
        val currentPlayerTurn: String,
        val currentRound: Int,
        val isFinished: Boolean,
        val battleUnitIds: Set<String>,
    )
}
