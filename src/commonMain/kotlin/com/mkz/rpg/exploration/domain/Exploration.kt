package com.mkz.rpg.exploration.domain

import com.mkz.rpg.exploration.domain.ExplorationError.AlreadyWalking
import com.mkz.rpg.exploration.domain.ExplorationError.NotExploring
import com.mkz.rpg.exploration.domain.ExplorationError.PartyRequired
import com.mkz.rpg.exploration.domain.ExplorationError.StepsRequired
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationPaused
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationStarted
import com.mkz.rpg.exploration.domain.ExplorationEvent.PartyWalked
import kotlin.jvm.JvmInline

/**
 * The human player exploring the battlefield with their party, out of the battles: the leader walks where the player
 * says, and the rest follow it in a line.
 *
 * The party walks one step at a time: each step is awaited until its battle unit occupies its tile, so the next battle
 * unit can step on the tile it left.
 */
@ConsistentCopyVisibility
data class Exploration private constructor(
    private val playerId: PlayerId,
    private val party: Party,
    private val mode: Mode,
    private val pendingSteps: PendingSteps,
    private val awaitedStep: Step?,
    private val events: Set<ExplorationEvent>,
) {
    companion object {
        /** Battle units: the party, the leader first. */
        fun start(
            playerId: String,
            battleUnitIds: List<String>,
        ): Exploration {
            if (battleUnitIds.isEmpty()) throw PartyRequired()
            return Exploration(
                playerId = PlayerId(playerId),
                party = Party(battleUnitIds),
                mode = Mode.EXPLORING,
                pendingSteps = PendingSteps(emptyList()),
                awaitedStep = null,
                events = setOf(ExplorationStarted(playerId = playerId, battleUnitIds = battleUnitIds)),
            )
        }
    }

    fun toDto() =
        Dto(
            playerId = playerId.value,
            leaderBattleUnitId = party.value.first(),
            battleUnitIds = party.value,
            isExploring = mode == Mode.EXPLORING,
            isWalking = isWalking(),
        )

    /** Whether the party can walk: it is exploring and has finished walking. */
    fun canWalk(): Boolean = mode == Mode.EXPLORING && !isWalking()

    /** Steps: where each battle unit of the party walks to, in walking order (the leader first). */
    fun walkParty(steps: List<Dto.StepDto>): Exploration {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isWalking()) throw AlreadyWalking()
        if (steps.isEmpty()) throw StepsRequired()
        return copy(pendingSteps = PendingSteps(steps.map(::Step)))
    }

    /** The next step to walk, or `null` while awaiting the previous one or when the party has finished walking. */
    fun nextStep(): Dto.StepDto? = if (awaitedStep == null) pendingSteps.value.firstOrNull()?.toDto() else null

    /** Awaits the next step, once its walk is requested. */
    fun awaitNextStep(): Exploration {
        val step = pendingSteps.value.first()
        return copy(awaitedStep = step, pendingSteps = PendingSteps(pendingSteps.value.drop(1)))
    }

    /** Skips the next step, e.g.: when its battle unit can not reach its tile. */
    fun skipNextStep(): Exploration = copy(pendingSteps = PendingSteps(pendingSteps.value.drop(1))).finishWalkWhenDone()

    /** Resolves the awaited step when the battle unit occupies its tile. Otherwise, nothing changes. */
    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Exploration {
        if (awaitedStep != Step(Dto.StepDto(battleUnitId = battleUnitId, row = row, column = column))) return this
        return copy(awaitedStep = null).finishWalkWhenDone()
    }

    /** Stops exploring, e.g.: when a battle starts. A walk in progress is abandoned. */
    fun pause(): Exploration =
        copy(
            mode = Mode.BATTLING,
            pendingSteps = PendingSteps(emptyList()),
            awaitedStep = null,
            events = events + ExplorationPaused(playerId = playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

    private fun isWalking(): Boolean = awaitedStep != null || pendingSteps.value.isNotEmpty()

    private fun finishWalkWhenDone(): Exploration {
        if (isWalking()) return this
        return copy(events = events + PartyWalked(playerId = playerId.value, battleUnitIds = party.value))
    }

    private enum class Mode {
        EXPLORING,
        BATTLING,
    }

    @JvmInline private value class PlayerId(
        val value: String,
    )

    @JvmInline private value class Party(
        val value: List<String>,
    )

    @JvmInline private value class PendingSteps(
        val value: List<Step>,
    )

    @JvmInline private value class Step(
        val value: Dto.StepDto,
    ) {
        fun toDto() = value
    }

    data class Dto(
        val playerId: String,
        val leaderBattleUnitId: String,
        val battleUnitIds: List<String>,
        val isExploring: Boolean,
        val isWalking: Boolean,
    ) {
        /** Where the leader walks to, one tile away. */
        enum class DirectionDto(
            val rowOffset: Int,
            val columnOffset: Int,
        ) {
            NORTH(rowOffset = -1, columnOffset = 0),
            EAST(rowOffset = 0, columnOffset = 1),
            SOUTH(rowOffset = 1, columnOffset = 0),
            WEST(rowOffset = 0, columnOffset = -1),
        }

        /** A battle unit walking to a tile. */
        data class StepDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )
    }
}
