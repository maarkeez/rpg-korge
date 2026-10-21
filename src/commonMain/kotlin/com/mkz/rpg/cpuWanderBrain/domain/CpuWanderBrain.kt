package com.mkz.rpg.cpuWanderBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestEngageEnemies
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestUpdateEngagement
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainError.AlreadyAwaitingAStep
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainError.NotTheNextBattleUnit
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent.BattleUnitsWanderedAfterRound
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring
import com.mkz.rpg.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

/** The party steps a defeated battle unit waits before it respawns on its home. */
const val RESPAWN_STEPS = 50

/**
 * The CPU battle units wandering around their home, the tile each one was deployed on, while they are out of a battle:
 * every second while the player explores, and when a battle round finishes.
 *
 * The battle units wander one after another: each one rests or steps to a tile next to it, and its step is awaited
 * until it occupies its tile, so the next one decides with the battlefield as it is then (e.g.: two battle units never
 * step on the same tile).
 *
 * A battle unit that leaves its territory (e.g.: chasing the party in a battle) returns: it heads all the way back home,
 * to the tile itself, before it wanders around it again.
 *
 * A defeated battle unit with a home counts the party steps since its defeat, and respawns on its home once it has
 * counted [RESPAWN_STEPS].
 */
@ConsistentCopyVisibility
data class CpuWanderBrain private constructor(
    private val homes: Homes,
    private val returningBattleUnits: ReturningBattleUnits,
    private val respawns: Respawns,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedStep: Step?,
    private val purpose: Purpose?,
    private val events: Set<DomainEvent>,
) {
    companion object {
        fun create(): CpuWanderBrain =
            CpuWanderBrain(
                homes = Homes(emptyMap()),
                returningBattleUnits = ReturningBattleUnits(emptySet()),
                respawns = Respawns(emptyMap()),
                pendingBattleUnits = PendingBattleUnits(emptyList()),
                awaitedStep = null,
                purpose = null,
                events = emptySet(),
            )
    }

    fun toDto() =
        Dto(
            homes = homes.value.map { (battleUnitId, home) -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) },
            returningBattleUnitIds = returningBattleUnits.value,
            pendingBattleUnitIds = pendingBattleUnits.value,
            isWandering = isWandering(),
            respawns = respawns.value.map { (battleUnitId, partySteps) -> Dto.RespawnDto(battleUnitId = battleUnitId, partySteps = partySteps) },
        )

    /** Remembers the tile the battle unit was deployed on as its home. A home never changes. */
    fun claimHome(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuWanderBrain {
        if (battleUnitId in homes.value) return this
        return copy(homes = Homes(homes.value + (battleUnitId to Home(row = row, column = column))))
    }

    fun homeOf(battleUnitId: String): Dto.HomeDto? = homes.value[battleUnitId]?.let { home -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) }

    /** The battle unit stands outside its territory, so it returns home. */
    fun leaveTerritory(battleUnitId: String): CpuWanderBrain = copy(returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value + battleUnitId))

    /** The battle unit stands at home, so it no longer returns: it wanders around it again. */
    fun arriveHome(battleUnitId: String): CpuWanderBrain = copy(returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value - battleUnitId))

    fun isReturning(battleUnitId: String): Boolean = battleUnitId in returningBattleUnits.value

    /** The battle unit is defeated: it starts counting the party steps, when it has a home. The others are ignored. */
    fun defeat(battleUnitId: String): CpuWanderBrain {
        if (battleUnitId !in homes.value) return this
        return copy(respawns = Respawns(respawns.value + (battleUnitId to 0)))
    }

    /** The party walked a step: every defeated battle unit counts it. */
    fun walkPartyStep(): CpuWanderBrain = copy(respawns = Respawns(respawns.value.mapValues { (_, partySteps) -> partySteps + 1 }))

    /** The defeated battle units that have counted [RESPAWN_STEPS] party steps, with their home, in their deployment order. */
    fun dueRespawns(): List<Dto.HomeDto> =
        homes.value
            .filterKeys { battleUnitId -> (respawns.value[battleUnitId] ?: 0) >= RESPAWN_STEPS }
            .map { (battleUnitId, home) -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) }

    /** The battle unit respawned on its home: it no longer counts, nor returns, as it stands at home. */
    fun respawn(battleUnitId: String): CpuWanderBrain =
        copy(
            respawns = Respawns(respawns.value - battleUnitId),
            returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value - battleUnitId),
        )

    /**
     * Battle units: those to wander, in order, while the player explores. The ones without a home are left out. A
     * wandering in progress is abandoned.
     */
    fun startWhileExploring(
        exploringPlayerId: String,
        battleUnitIds: List<String>,
    ): CpuWanderBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(battleUnitIds.filter { battleUnitId -> battleUnitId in homes.value }),
            awaitedStep = null,
            purpose = Purpose.Exploring(exploringPlayerId),
        ).finishWhenDone()

    /**
     * Battle units in the battle: the ones that do not wander when a round finishes. The other battle units with a home
     * wander, in the order they were deployed. A wandering in progress is abandoned.
     */
    fun startAfterRound(battleUnitIdsInBattle: Set<String>): CpuWanderBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(homes.value.keys.filter { battleUnitId -> battleUnitId !in battleUnitIdsInBattle }),
            awaitedStep = null,
            purpose = Purpose.AfterRound(battleUnitIdsInBattle),
        ).finishWhenDone()

    /** The battle unit to wander next, or `null` while awaiting the previous step or when every battle unit has wandered. */
    fun nextBattleUnitId(): String? = if (awaitedStep == null) pendingBattleUnits.value.firstOrNull() else null

    /** The next battle unit rests, so it has wandered. */
    fun rest(battleUnitId: String): CpuWanderBrain {
        requireNextBattleUnit(battleUnitId)
        return copy(pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1))).finishWhenDone()
    }

    /** The next battle unit steps to the tile, which is awaited until it occupies it. */
    fun requestStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuWanderBrain {
        requireNextBattleUnit(battleUnitId)
        return copy(
            pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1)),
            awaitedStep = Step(battleUnitId = battleUnitId, row = row, column = column),
        )
    }

    /** Resolves the awaited step when the battle unit occupies its tile. Otherwise, nothing changes. */
    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuWanderBrain {
        if (awaitedStep != Step(battleUnitId = battleUnitId, row = row, column = column)) return this
        return copy(awaitedStep = null).finishWhenDone()
    }

    fun pullEvents() = events to copy(events = emptySet())

    private fun isWandering(): Boolean = awaitedStep != null || pendingBattleUnits.value.isNotEmpty()

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (awaitedStep != null) throw AlreadyAwaitingAStep()
        if (pendingBattleUnits.value.firstOrNull() != battleUnitId) throw NotTheNextBattleUnit()
    }

    // Once every battle unit has wandered, it announces it, and requests what follows from the CPU battle brain right
    // after: engaging the exploring player, or updating the engagement of the battle.
    private fun finishWhenDone(): CpuWanderBrain {
        if (isWandering()) return this
        val finishedEvents =
            when (purpose) {
                is Purpose.Exploring ->
                    listOf(
                        BattleUnitsWanderedWhileExploring(exploringPlayerId = purpose.exploringPlayerId),
                        RequestEngageEnemies(exploringPlayerId = purpose.exploringPlayerId),
                    )
                is Purpose.AfterRound ->
                    listOf(
                        BattleUnitsWanderedAfterRound(battleUnitIds = purpose.battleUnitIdsInBattle),
                        RequestUpdateEngagement(battleUnitIds = purpose.battleUnitIdsInBattle),
                    )
                null -> return this
            }
        return copy(events = events + finishedEvents)
    }

    private data class Home(
        val row: Int,
        val column: Int,
    )

    private data class Step(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    )

    // Why the battle units wander, which decides what follows once they have.
    private sealed interface Purpose {
        data class Exploring(
            val exploringPlayerId: String,
        ) : Purpose

        data class AfterRound(
            val battleUnitIdsInBattle: Set<String>,
        ) : Purpose
    }

    @JvmInline private value class Homes(
        val value: Map<String, Home>,
    )

    @JvmInline private value class ReturningBattleUnits(
        val value: Set<String>,
    )

    // The party steps each defeated battle unit has counted since its defeat.
    @JvmInline private value class Respawns(
        val value: Map<String, Int>,
    )

    @JvmInline private value class PendingBattleUnits(
        val value: List<String>,
    )

    data class Dto(
        val homes: List<HomeDto>,
        val returningBattleUnitIds: Set<String>,
        val pendingBattleUnitIds: List<String>,
        val isWandering: Boolean,
        val respawns: List<RespawnDto>,
    ) {
        data class HomeDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )

        data class RespawnDto(
            val battleUnitId: String,
            val partySteps: Int,
        )
    }
}
