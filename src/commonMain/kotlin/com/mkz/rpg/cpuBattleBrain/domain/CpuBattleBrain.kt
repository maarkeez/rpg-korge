package com.mkz.rpg.cpuBattleBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.AlreadyAwaitingADecision
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.NoBattleUnitsToDecideFor
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDecisionResolved
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDone
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnFinished
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnStarted
import kotlin.jvm.JvmInline

/**
 * Plays a CPU player turn one decision at a time: it decides for its pending battle units in order, and waits for each
 * requested decision to be resolved before making the next one, so every decision is based on the state left by the
 * previous one.
 */
@ConsistentCopyVisibility
data class CpuBattleBrain private constructor(
    private val playerId: PlayerId,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedDecision: AwaitedDecision?,
    private val decisionsTaken: DecisionsTaken,
    private val events: Set<CpuBattleBrainEvent>,
) {
    companion object {
        // Guards against endless player turns: every decision spends casts or steps, so a player turn should always end before.
        const val MAX_DECISIONS_PER_PLAYER_TURN = 20

        fun startPlayerTurn(
            playerId: String,
            battleUnitIds: List<String>,
        ): CpuBattleBrain {
            if (battleUnitIds.isEmpty()) throw NoBattleUnitsToDecideFor()
            return CpuBattleBrain(
                playerId = PlayerId(playerId),
                pendingBattleUnits = PendingBattleUnits(battleUnitIds),
                awaitedDecision = null,
                decisionsTaken = DecisionsTaken(0),
                events = setOf(CpuPlayerTurnStarted(playerId)),
            )
        }
    }

    fun toDto() =
        Dto(
            playerId = playerId.value,
            pendingBattleUnitIds = pendingBattleUnits.value,
            isAwaitingDecision = awaitedDecision != null,
            decisionsTaken = decisionsTaken.value,
        )

    /** The battle unit to decide for next, or `null` when every battle unit is done. */
    fun nextBattleUnitId(): String? = pendingBattleUnits.value.firstOrNull()

    fun hasReachedDecisionLimit(): Boolean = decisionsTaken.value >= MAX_DECISIONS_PER_PLAYER_TURN

    fun requestWalk(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBattleBrain = requestDecision(AwaitedDecision.Walk(battleUnitId = battleUnitId, row = row, column = column))

    fun requestCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBattleBrain = requestDecision(AwaitedDecision.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Resolves the awaited walk when the battle unit occupies the requested tile. Otherwise, nothing changes. */
    fun resolveWalk(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBattleBrain = resolveDecision(AwaitedDecision.Walk(battleUnitId = battleUnitId, row = row, column = column))

    /** Resolves the awaited cast when the battle unit ability has been resolved. Otherwise, nothing changes. */
    fun resolveCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBattleBrain = resolveDecision(AwaitedDecision.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Marks the next battle unit as done, once it has nothing left to decide this player turn. */
    fun markDone(battleUnitId: String): CpuBattleBrain {
        requireNextBattleUnit(battleUnitId)
        if (awaitedDecision != null) throw AlreadyAwaitingADecision()
        return copy(
            pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1)),
            events = events + CpuBattleUnitDone(playerId = playerId.value, battleUnitId = battleUnitId),
        )
    }

    /** Whether the player turn is being played: some battle units are still pending. */
    fun isPlayingPlayerTurn(): Boolean = pendingBattleUnits.value.isNotEmpty()

    /**
     * Leaves the battle unit pending after the ones already pending, e.g.: a battle unit summoned during the player turn,
     * which plays it too. Nothing changes once the player turn is over, nor when it is already pending.
     */
    fun addBattleUnit(battleUnitId: String): CpuBattleBrain {
        if (!isPlayingPlayerTurn() || battleUnitId in pendingBattleUnits.value) return this
        return copy(pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value + battleUnitId))
    }

    fun finishPlayerTurn(): CpuBattleBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(emptyList()),
            awaitedDecision = null,
            events = events + CpuPlayerTurnFinished(playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

    private fun requestDecision(decision: AwaitedDecision): CpuBattleBrain {
        requireNextBattleUnit(decision.battleUnitId)
        if (awaitedDecision != null) throw AlreadyAwaitingADecision()
        return copy(
            awaitedDecision = decision,
            decisionsTaken = DecisionsTaken(decisionsTaken.value + 1),
        )
    }

    private fun resolveDecision(resolvedDecision: AwaitedDecision): CpuBattleBrain {
        if (awaitedDecision != resolvedDecision) return this
        return copy(
            awaitedDecision = null,
            events = events + CpuBattleUnitDecisionResolved(playerId = playerId.value, battleUnitId = resolvedDecision.battleUnitId),
        )
    }

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (nextBattleUnitId() != battleUnitId) throw CpuBattleBrainError.NotTheNextBattleUnit()
    }

    private sealed interface AwaitedDecision {
        val battleUnitId: String

        data class Walk(
            override val battleUnitId: String,
            val row: Int,
            val column: Int,
        ) : AwaitedDecision

        data class Cast(
            override val battleUnitId: String,
            val abilityId: String,
        ) : AwaitedDecision
    }

    @JvmInline private value class PlayerId(
        val value: String,
    )

    @JvmInline private value class PendingBattleUnits(
        val value: List<String>,
    )

    @JvmInline private value class DecisionsTaken(
        val value: Int,
    )

    data class Dto(
        val playerId: String,
        val pendingBattleUnitIds: List<String>,
        val isAwaitingDecision: Boolean,
        val decisionsTaken: Int,
    )
}
