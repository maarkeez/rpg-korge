package com.mkz.rpg.wandering.domain

import com.mkz.rpg.wandering.domain.WanderingError.AlreadyAwaitingAStep
import com.mkz.rpg.wandering.domain.WanderingError.NotTheNextBattleUnit
import com.mkz.rpg.wandering.domain.WanderingEvent.BattleUnitsWanderedWhileExploring
import kotlin.jvm.JvmInline

/**
 * The CPU battle units wandering around their home, the tile each one was deployed on, while they are out of a battle.
 *
 * The battle units wander one after another: each one rests or steps to a tile next to it, and its step is awaited
 * until it occupies its tile, so the next one decides with the battlefield as it is then (e.g.: two battle units never
 * step on the same tile).
 */
@ConsistentCopyVisibility
data class Wandering private constructor(
    private val homes: Homes,
    private val pendingBattleUnits: PendingBattleUnits,
    private val awaitedStep: Step?,
    private val purpose: Purpose?,
    private val events: Set<WanderingEvent>,
) {
    companion object {
        fun create(): Wandering =
            Wandering(
                homes = Homes(emptyMap()),
                pendingBattleUnits = PendingBattleUnits(emptyList()),
                awaitedStep = null,
                purpose = null,
                events = emptySet(),
            )
    }

    fun toDto() =
        Dto(
            homes = homes.value.map { (battleUnitId, home) -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) },
            pendingBattleUnitIds = pendingBattleUnits.value,
            isWandering = isWandering(),
        )

    /** Remembers the tile the battle unit was deployed on as its home. A home never changes. */
    fun claimHome(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Wandering {
        if (battleUnitId in homes.value) return this
        return copy(homes = Homes(homes.value + (battleUnitId to Home(row = row, column = column))))
    }

    fun homeOf(battleUnitId: String): Dto.HomeDto? = homes.value[battleUnitId]?.let { home -> Dto.HomeDto(battleUnitId = battleUnitId, row = home.row, column = home.column) }

    /**
     * Battle units: those to wander, in order, while the player explores. The ones without a home are left out. A
     * wandering in progress is abandoned.
     */
    fun startWhileExploring(
        exploringPlayerId: String,
        battleUnitIds: List<String>,
    ): Wandering =
        copy(
            pendingBattleUnits = PendingBattleUnits(battleUnitIds.filter { battleUnitId -> battleUnitId in homes.value }),
            awaitedStep = null,
            purpose = Purpose.Exploring(exploringPlayerId),
        ).finishWhenDone()

    /** The battle unit to wander next, or `null` while awaiting the previous step or when every battle unit has wandered. */
    fun nextBattleUnitId(): String? = if (awaitedStep == null) pendingBattleUnits.value.firstOrNull() else null

    /** The next battle unit rests, so it has wandered. */
    fun rest(battleUnitId: String): Wandering {
        requireNextBattleUnit(battleUnitId)
        return copy(pendingBattleUnits = PendingBattleUnits(pendingBattleUnits.value.drop(1))).finishWhenDone()
    }

    /** The next battle unit steps to the tile, which is awaited until it occupies it. */
    fun requestStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Wandering {
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
    ): Wandering {
        if (awaitedStep != Step(battleUnitId = battleUnitId, row = row, column = column)) return this
        return copy(awaitedStep = null).finishWhenDone()
    }

    fun pullEvents() = events to copy(events = emptySet())

    private fun isWandering(): Boolean = awaitedStep != null || pendingBattleUnits.value.isNotEmpty()

    private fun requireNextBattleUnit(battleUnitId: String) {
        if (awaitedStep != null) throw AlreadyAwaitingAStep()
        if (pendingBattleUnits.value.firstOrNull() != battleUnitId) throw NotTheNextBattleUnit()
    }

    // Once every battle unit has wandered, it announces it, for what follows (e.g.: engaging the exploring player).
    private fun finishWhenDone(): Wandering {
        if (isWandering()) return this
        val finishedEvent =
            when (purpose) {
                is Purpose.Exploring -> BattleUnitsWanderedWhileExploring(exploringPlayerId = purpose.exploringPlayerId)
                null -> return this
            }
        return copy(events = events + finishedEvent)
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
    }

    @JvmInline private value class Homes(
        val value: Map<String, Home>,
    )

    @JvmInline private value class PendingBattleUnits(
        val value: List<String>,
    )

    data class Dto(
        val homes: List<HomeDto>,
        val pendingBattleUnitIds: List<String>,
        val isWandering: Boolean,
    ) {
        data class HomeDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )
    }
}
