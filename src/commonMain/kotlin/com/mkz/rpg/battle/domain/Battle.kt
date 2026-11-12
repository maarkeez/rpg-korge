package com.mkz.rpg.battle.domain

import com.mkz.rpg.battle.domain.BattleError.MinimumTwoPlayersRequired
import com.mkz.rpg.battle.domain.BattleEvent.BattleDisengaged
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnFinished
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.RoundFinished
import com.mkz.rpg.battle.domain.BattleEvent.RoundStarted
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Battle private constructor(
    private val turnOrder: TurnOrder,
    private val battleUnits: BattleUnits,
    private val currentRound: CurrentRound,
    private val currentPlayerId: CurrentPlayerId,
    private val isFinished: IsFinished,
    private val events: Set<BattleEvent>,
) {
    companion object {
        fun startFirstRound(
            players: List<String>,
            battleUnits: Map<String, String>,
        ): Battle {
            if (players.size < 2) throw MinimumTwoPlayersRequired()
            val battleUnitIds = battleUnits.keys
            return Battle(
                turnOrder = TurnOrder(players),
                battleUnits = BattleUnits(battleUnits),
                currentRound = CurrentRound(1),
                currentPlayerId = CurrentPlayerId(players.first()),
                isFinished = IsFinished(false),
                events = setOf(BattleStarted, PlayerTurnStarted(players.first(), battleUnitIds)),
            )
        }
    }

    fun toDto() =
        Dto(
            currentPlayerId = currentPlayerId.value,
            currentRound = currentRound.value,
            isFinished = isFinished.value,
            battleUnitIds = battleUnits.value.keys,
        )

    fun startNextRound(): Battle {
        val nextRound = CurrentRound(currentRound.value + 1)
        val nextPlayerId = CurrentPlayerId(turnOrder.value.first())
        return copy(
            currentRound = nextRound,
            currentPlayerId = CurrentPlayerId(turnOrder.value.first()),
            events =
                events +
                    setOf(
                        RoundStarted(nextRound.value, battleUnits.value.keys),
                        PlayerTurnStarted(nextPlayerId.value, battleUnits.value.keys),
                    ),
        )
    }

    fun finishPlayerTurn(): Battle {
        val currentPlayerIndex = turnOrder.value.indexOf(currentPlayerId.value)
        val nextPlayerIndex = currentPlayerIndex + 1
        val roundFinished = nextPlayerIndex >= turnOrder.value.size
        val playerTurnFinished = PlayerTurnFinished(currentPlayerId.value, battleUnits.value.keys)
        if (roundFinished) {
            return copy(
                events = events + setOf(playerTurnFinished, RoundFinished(currentRound.value, battleUnits.value.keys)),
            )
        } else {
            val nextPlayerId = turnOrder.value[nextPlayerIndex]
            return copy(
                currentPlayerId = CurrentPlayerId(nextPlayerId),
                events = events + setOf(playerTurnFinished, PlayerTurnStarted(nextPlayerId, battleUnits.value.keys)),
            )
        }
    }

    fun join(
        battleUnitId: String,
        playerId: String,
    ): Battle = copy(battleUnits = BattleUnits(battleUnits.value + (battleUnitId to playerId)))

    fun leave(battleUnitId: String): Battle = copy(battleUnits = BattleUnits(battleUnits.value - battleUnitId))

    fun leaveDuringRound(battleUnitId: String): Battle {
        val leftBattle = leave(battleUnitId)
        val canDisengage = !isFinished.value && !isDisengaged()
        val disengages = canDisengage && battleUnitId in battleUnits.value && leftBattle.isDisengaged()
        return if (disengages) leftBattle.copy(events = events + BattleDisengaged(leftBattle.battleUnits.value.keys)) else leftBattle
    }

    fun isDisengaged(): Boolean {
        val playersInBattle = battleUnits.value.values.toSet()
        return turnOrder.value.count { playerId -> playerId in playersInBattle } < 2
    }

    fun finish(): Battle =
        copy(
            isFinished = IsFinished(true),
            events = events + BattleFinished,
        )

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class TurnOrder(
        val value: List<String>,
    )

    @JvmInline private value class BattleUnits(
        val value: Map<String, String>,
    )

    @JvmInline private value class CurrentRound(
        val value: Int,
    )

    @JvmInline private value class CurrentPlayerId(
        val value: String,
    )

    @JvmInline private value class IsFinished(
        val value: Boolean,
    )

    data class Dto(
        val currentPlayerId: String,
        val currentRound: Int,
        val isFinished: Boolean,
        val battleUnitIds: Set<String>,
    )
}
