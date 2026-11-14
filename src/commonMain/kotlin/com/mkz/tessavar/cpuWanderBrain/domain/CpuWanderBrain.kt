package com.mkz.tessavar.cpuWanderBrain.domain

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestEngageEnemies
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestJoinAndLeaveBattle
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainError.AlreadyAwaitingAStep
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainError.NotTheNextBattleUnit
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainEvent.CpuBattleUnitsWanderedWhileExploring
import com.mkz.tessavar.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

const val RESPAWN_STEPS = 50

@ConsistentCopyVisibility
data class CpuWanderBrain private constructor(
    private val homes: Homes,
    private val returningBattleUnits: ReturningBattleUnits,
    private val awaitedRespawns: AwaitedRespawns,
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
                awaitedRespawns = AwaitedRespawns(emptyMap()),
                pendingBattleUnits = PendingBattleUnits(emptyList()),
                awaitedStep = null,
                purpose = null,
                events = emptySet(),
            )

        fun load(snapshot: Dto.SnapshotDto): CpuWanderBrain =
            CpuWanderBrain(
                homes =
                    Homes(
                        snapshot.homes.associate { home ->
                            home.battleUnitId to
                                Home(
                                    row = home.row,
                                    column = home.column,
                                    distancesFromHome = home.distancesFromHome.associate { distance -> PositionDto(row = distance.row, column = distance.column) to distance.distance },
                                )
                        },
                    ),
                returningBattleUnits = ReturningBattleUnits(snapshot.returningBattleUnitIds),
                awaitedRespawns = AwaitedRespawns(snapshot.awaitedRespawns.associate { respawn -> respawn.battleUnitId to respawn.partySteps }),
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
            awaitedRespawns = awaitedRespawns.value.map { (battleUnitId, partySteps) -> Dto.AwaitedRespawnDto(battleUnitId = battleUnitId, partySteps = partySteps) },
        )

    fun toSnapshot() =
        Dto.SnapshotDto(
            homes =
                homes.value.map { (battleUnitId, home) ->
                    Dto.HomeSnapshotDto(
                        battleUnitId = battleUnitId,
                        row = home.row,
                        column = home.column,
                        distancesFromHome = home.distancesFromHome.map { (position, distance) -> Dto.DistanceDto(row = position.row, column = position.column, distance = distance) },
                    )
                },
            returningBattleUnitIds = returningBattleUnits.value,
            awaitedRespawns = awaitedRespawns.value.map { (battleUnitId, partySteps) -> Dto.AwaitedRespawnDto(battleUnitId = battleUnitId, partySteps = partySteps) },
        )

    fun claimHome(
        battleUnitId: String,
        row: Int,
        column: Int,
        distancesFromHome: Map<PositionDto, Int>,
    ): CpuWanderBrain {
        if (battleUnitId in homes.value) return this
        return copy(homes = Homes(homes.value + (battleUnitId to Home(row = row, column = column, distancesFromHome = distancesFromHome))))
    }

    fun homeOf(battleUnitId: String): Dto.HomeDto? = homes.value[battleUnitId]?.let { home -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) }

    fun distancesFromHome(battleUnitId: String): Map<PositionDto, Int> = homes.value[battleUnitId]?.distancesFromHome.orEmpty()

    fun leaveTerritory(battleUnitId: String): CpuWanderBrain = copy(returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value + battleUnitId))

    fun arriveHome(battleUnitId: String): CpuWanderBrain = copy(returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value - battleUnitId))

    fun isReturning(battleUnitId: String): Boolean = battleUnitId in returningBattleUnits.value

    fun awaitRespawn(battleUnitId: String): CpuWanderBrain {
        if (battleUnitId !in homes.value) return this
        return copy(awaitedRespawns = AwaitedRespawns(awaitedRespawns.value + (battleUnitId to 0)))
    }

    fun walkPartyStep(): CpuWanderBrain = copy(awaitedRespawns = AwaitedRespawns(awaitedRespawns.value.mapValues { (_, partySteps) -> partySteps + 1 }))

    fun dueRespawns(): List<Dto.HomeDto> =
        homes.value
            .filterKeys { battleUnitId -> (awaitedRespawns.value[battleUnitId] ?: 0) >= RESPAWN_STEPS }
            .map { (battleUnitId, home) -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) }

    fun respawn(battleUnitId: String): CpuWanderBrain =
        copy(
            awaitedRespawns = AwaitedRespawns(awaitedRespawns.value - battleUnitId),
            returningBattleUnits = ReturningBattleUnits(returningBattleUnits.value - battleUnitId),
        )

    fun startWhileExploring(
        exploringPlayerId: String,
        battleUnitIds: List<String>,
    ): CpuWanderBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(battleUnitIds.filter { battleUnitId -> battleUnitId in homes.value }),
            awaitedStep = null,
            purpose = Purpose.Exploring(exploringPlayerId),
        ).finishWhenDone()

    fun startAfterRound(
        battleUnitIdsInBattle: Set<String>,
        battleUnitIds: List<String>,
    ): CpuWanderBrain =
        copy(
            pendingBattleUnits = PendingBattleUnits(battleUnitIds.filter { battleUnitId -> battleUnitId in homes.value && battleUnitId !in battleUnitIdsInBattle }),
            awaitedStep = null,
            purpose = Purpose.AfterRound(battleUnitIdsInBattle),
        ).finishWhenDone()

    fun nextBattleUnitId(): String? = if (awaitedStep == null) pendingBattleUnits.value.firstOrNull() else null

    fun rest(battleUnitId: String): CpuWanderBrain {
        requireNextBattleUnit(battleUnitId)
        return copy(pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1))).finishWhenDone()
    }

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

    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): CpuWanderBrain {
        if (awaitedStep != Step(battleUnitId = battleUnitId, row = row, column = column)) return this
        return copy(awaitedStep = null).finishWhenDone()
    }

    fun pullEvents(): Pair<Set<DomainEvent>, CpuWanderBrain> = events to copy(events = emptySet())

    private fun isWandering(): Boolean = awaitedStep != null || pendingBattleUnits.value.isNotEmpty()

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (awaitedStep != null) throw AlreadyAwaitingAStep()
        if (pendingBattleUnits.value.firstOrNull() != battleUnitId) throw NotTheNextBattleUnit()
    }

    private fun finishWhenDone(): CpuWanderBrain {
        if (isWandering()) return this
        val finishedEvents =
            when (purpose) {
                is Purpose.Exploring ->
                    listOf(
                        CpuBattleUnitsWanderedWhileExploring(exploringPlayerId = purpose.exploringPlayerId),
                        RequestEngageEnemies(exploringPlayerId = purpose.exploringPlayerId),
                    )
                is Purpose.AfterRound ->
                    listOf(
                        CpuBattleUnitsWanderedAfterRound(battleUnitIds = purpose.battleUnitIdsInBattle),
                        RequestJoinAndLeaveBattle(battleUnitIds = purpose.battleUnitIdsInBattle),
                    )
                null -> return this
            }
        return copy(events = events + finishedEvents)
    }

    private data class Home(
        val row: Int,
        val column: Int,
        val distancesFromHome: Map<PositionDto, Int>,
    )

    private data class Step(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    )

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

    @JvmInline private value class AwaitedRespawns(
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
        val awaitedRespawns: List<AwaitedRespawnDto>,
    ) {
        data class HomeDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )

        data class AwaitedRespawnDto(
            val battleUnitId: String,
            val partySteps: Int,
        )

        data class SnapshotDto(
            val homes: List<HomeSnapshotDto>,
            val returningBattleUnitIds: Set<String>,
            val awaitedRespawns: List<AwaitedRespawnDto>,
        )

        data class HomeSnapshotDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
            val distancesFromHome: List<DistanceDto>,
        )

        data class DistanceDto(
            val row: Int,
            val column: Int,
            val distance: Int,
        )
    }
}
