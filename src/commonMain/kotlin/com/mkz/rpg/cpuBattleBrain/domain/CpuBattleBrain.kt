package com.mkz.rpg.cpuBattleBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.AlreadyAwaitingADecision
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.NoBattleUnitsToDecideFor
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDecisionResolved
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDone
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnFinished
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnStarted
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class CpuBattleBrain private constructor(
    private val playerId: PlayerId,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedDecision: AwaitedDecision?,
    private val decisionsTaken: DecisionsTaken,
    private val events: Set<CpuBattleBrainEvent>,
) {
    companion object {
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

    fun resolveWalk(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBattleBrain = resolveDecision(AwaitedDecision.Walk(battleUnitId = battleUnitId, row = row, column = column))

    fun resolveCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBattleBrain = resolveDecision(AwaitedDecision.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    fun markDone(battleUnitId: String): CpuBattleBrain {
        requireNextBattleUnit(battleUnitId)
        if (awaitedDecision != null) throw AlreadyAwaitingADecision()
        return copy(
            pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1)),
            events = events + CpuBattleUnitDone(playerId = playerId.value, battleUnitId = battleUnitId),
        )
    }

    fun isPlayingPlayerTurn(): Boolean = pendingBattleUnits.value.isNotEmpty()

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
