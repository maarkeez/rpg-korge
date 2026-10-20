package com.mkz.rpg.cpuBattleBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.AlreadyAwaitingAnAction
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.NoBattleUnitsToManage
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleBrainInitialized
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuTurnFinished
import kotlin.jvm.JvmInline

/**
 * Plays a CPU turn one action at a time: it manages its battle units in order, and waits for each requested
 * action to be resolved before deciding the next one, so every decision is based on the state left by the previous one.
 */
@ConsistentCopyVisibility
data class CpuBattleBrain private constructor(
    private val playerId: PlayerId,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedAction: AwaitedAction?,
    private val actionsTaken: ActionsTaken,
    private val events: Set<CpuBattleBrainEvent>,
) {
    companion object {
        // Guards against endless turns: every action spends casts or steps, so a turn should always end before.
        const val MAX_ACTIONS_PER_TURN = 20

        fun startTurn(
            playerId: String,
            battleUnitIds: List<String>,
        ): CpuBattleBrain {
            if (battleUnitIds.isEmpty()) throw NoBattleUnitsToManage()
            return CpuBattleBrain(
                playerId = PlayerId(playerId),
                pendingBattleUnits = PendingBattleUnits(battleUnitIds),
                awaitedAction = null,
                actionsTaken = ActionsTaken(0),
                events = setOf(CpuBattleBrainInitialized(playerId)),
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
    ): CpuBattleBrain = requestAction(AwaitedAction.Move(battleUnitId = battleUnitId, row = row, column = column))

    fun requestCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBattleBrain = requestAction(AwaitedAction.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Resolves the awaited move when the battle unit occupies the requested tile. Otherwise, nothing changes. */
    fun resolveMove(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuBattleBrain = resolveAction(AwaitedAction.Move(battleUnitId = battleUnitId, row = row, column = column))

    /** Resolves the awaited cast when the battle unit ability has been resolved. Otherwise, nothing changes. */
    fun resolveCast(
        battleUnitId: String,
        abilityId: String,
    ): CpuBattleBrain = resolveAction(AwaitedAction.Cast(battleUnitId = battleUnitId, abilityId = abilityId))

    /** Marks the next battle unit as managed, once it has nothing left to do this turn. */
    fun manageBattleUnit(battleUnitId: String): CpuBattleBrain {
        requireNextBattleUnit(battleUnitId)
        if (awaitedAction != null) throw AlreadyAwaitingAnAction()
        return copy(
            pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1)),
            events = events + CpuBattleUnitManaged(playerId = playerId.value, battleUnitId = battleUnitId),
        )
    }

    /** Whether the turn is being played: some battle units are still to be managed. */
    fun isPlayingTurn(): Boolean = pendingBattleUnits.value.isNotEmpty()

    /**
     * Manages the battle unit after the ones already pending, e.g.: a battle unit summoned during the turn, which plays
     * it too. Nothing changes once the turn is over, nor when it is already pending.
     */
    fun addBattleUnit(battleUnitId: String): CpuBattleBrain {
        if (!isPlayingTurn() || battleUnitId in pendingBattleUnits.value) return this
        return copy(pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value + battleUnitId))
    }

    fun finishTurn(): CpuBattleBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(emptyList()),
            awaitedAction = null,
            events = events + CpuTurnFinished(playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

    private fun requestAction(action: AwaitedAction): CpuBattleBrain {
        requireNextBattleUnit(action.battleUnitId)
        if (awaitedAction != null) throw AlreadyAwaitingAnAction()
        return copy(
            awaitedAction = action,
            actionsTaken = ActionsTaken(actionsTaken.value + 1),
        )
    }

    private fun resolveAction(resolvedAction: AwaitedAction): CpuBattleBrain {
        if (awaitedAction != resolvedAction) return this
        return copy(
            awaitedAction = null,
            events = events + CpuBattleUnitActionResolved(playerId = playerId.value, battleUnitId = resolvedAction.battleUnitId),
        )
    }

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (nextBattleUnitId() != battleUnitId) throw CpuBattleBrainError.NotTheNextBattleUnit()
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
