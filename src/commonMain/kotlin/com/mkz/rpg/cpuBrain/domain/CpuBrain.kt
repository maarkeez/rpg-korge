package com.mkz.rpg.cpuBrain.domain

import com.mkz.rpg.cpuBrain.domain.CpuBrainError.AlreadyAwaitingAnAction
import com.mkz.rpg.cpuBrain.domain.CpuBrainError.NoBattleUnitsToManage
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBrainInitialized
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuTurnFinished
import kotlin.jvm.JvmInline

/**
 * Plays a CPU turn one action at a time: it manages its battle units in order, and waits for each requested
 * action to be resolved before deciding the next one, so every decision is based on the state left by the previous one.
 */
@ConsistentCopyVisibility
data class CpuBrain private constructor(
    private val playerId: PlayerId,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedAction: AwaitedAction?,
    private val actionsTaken: ActionsTaken,
    private val events: Set<CpuBrainEvent>,
) {
    companion object {
        // Guards against endless turns: every action spends casts or steps, so a turn should always end before.
        const val MAX_ACTIONS_PER_TURN = 20

        fun startTurn(
            playerId: String,
            battleUnitIds: List<String>,
        ): CpuBrain {
            if (battleUnitIds.isEmpty()) throw NoBattleUnitsToManage()
            return CpuBrain(
                playerId = PlayerId(playerId),
                pendingBattleUnits = PendingBattleUnits(battleUnitIds),
                awaitedAction = null,
                actionsTaken = ActionsTaken(0),
                events = setOf(CpuBrainInitialized(playerId)),
            )
        }
    }

    fun toDto() =
        Dto(
            playerId = playerId.value,
            pendingBattleUnitIds = pendingBattleUnits.value,
            isAwaitingAction = awaitedAction != null,
            actionsTaken = actionsTaken.value,
        )

    /** The battle unit to manage next, or `null` when every battle unit has been managed. */
    fun nextBattleUnitId(): String? = pendingBattleUnits.value.firstOrNull()

    fun hasReachedActionLimit(): Boolean = actionsTaken.value >= MAX_ACTIONS_PER_TURN

    fun requestMove(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBrain = requestAction(AwaitedAction.Move(battleUnitId = battleUnitId, row = row, column = column))

    fun requestCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBrain = requestAction(AwaitedAction.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Resolves the awaited move when the battle unit occupies the requested tile. Otherwise, nothing changes. */
    fun resolveMove(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBrain = resolveAction(AwaitedAction.Move(battleUnitId = battleUnitId, row = row, column = column))

    /** Resolves the awaited cast when the battle unit ability has been resolved. Otherwise, nothing changes. */
    fun resolveCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBrain = resolveAction(AwaitedAction.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Marks the next battle unit as managed, once it has nothing left to do this turn. */
    fun manageBattleUnit(battleUnitId: String): CpuBrain {
        requireNextBattleUnit(battleUnitId)
        if (awaitedAction != null) throw AlreadyAwaitingAnAction()
        return copy(
            pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1)),
            events = events + CpuBattleUnitManaged(playerId = playerId.value, battleUnitId = battleUnitId),
        )
    }

    fun finishTurn(): CpuBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(emptyList()),
            awaitedAction = null,
            events = events + CpuTurnFinished(playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

    private fun requestAction(action: AwaitedAction): CpuBrain {
        requireNextBattleUnit(action.battleUnitId)
        if (awaitedAction != null) throw AlreadyAwaitingAnAction()
        return copy(
            awaitedAction = action,
            actionsTaken = ActionsTaken(actionsTaken.value + 1),
        )
    }

    private fun resolveAction(resolvedAction: AwaitedAction): CpuBrain {
        if (awaitedAction != resolvedAction) return this
        return copy(
            awaitedAction = null,
            events = events + CpuBattleUnitActionResolved(playerId = playerId.value, battleUnitId = resolvedAction.battleUnitId),
        )
    }

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (nextBattleUnitId() != battleUnitId) throw CpuBrainError.NotTheNextBattleUnit()
    }

    private sealed interface AwaitedAction {
        val battleUnitId: String

        data class Move(
            override val battleUnitId: String,
            val row: Int,
            val column: Int,
        ) : AwaitedAction

        data class Cast(
            override val battleUnitId: String,
            val abilityId: String,
        ) : AwaitedAction
    }

    @JvmInline private value class PlayerId(
        val value: String,
    )

    @JvmInline private value class PendingBattleUnits(
        val value: List<String>,
    )

    @JvmInline private value class ActionsTaken(
        val value: Int,
    )

    data class Dto(
        val playerId: String,
        val pendingBattleUnitIds: List<String>,
        val isAwaitingAction: Boolean,
        val actionsTaken: Int,
    )
}
