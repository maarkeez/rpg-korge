package com.mkz.rpg.party.domain

import com.mkz.rpg.party.domain.PartyError.AlreadyWalking
import com.mkz.rpg.party.domain.PartyError.NotAFollower
import com.mkz.rpg.party.domain.PartyError.NotExploring
import com.mkz.rpg.party.domain.PartyError.NotTheParty
import com.mkz.rpg.party.domain.PartyError.PartyRequired
import com.mkz.rpg.party.domain.PartyError.StepsRequired
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationSecondStarted
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.party.domain.PartyEvent.FormationClosed
import com.mkz.rpg.party.domain.PartyEvent.FormationOpened
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkStarted
import com.mkz.rpg.party.domain.PartyEvent.PartyJoined
import com.mkz.rpg.party.domain.PartyEvent.PartyWalked
import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * The human player exploring the battlefield with their party, out of the battles: the leader walks where the player
 * says, and the rest follow it in a line.
 *
 * The party walks one step at a time: each step is awaited until its battle unit occupies its tile, so the next battle
 * unit can step on the tile it left.
 *
 * The followers walk in line order, which is kept apart from the party order: the leader swaps tiles with a party member
 * on its next tile, and the line is rebuilt then, so each follower follows the closest member ahead of it.
 *
 * Every second while exploring, the battle units that stood still during it receive the effect of their terrain, so it
 * remembers the battle units that moved during the current second.
 *
 * The party can talk to an npc while exploring. Time stops while talking: the party does not walk, and no second starts.
 * Time also stops while the player sets the party order in the formation, which decides who leads.
 */
@ConsistentCopyVisibility
data class Party private constructor(
    private val playerId: PlayerId,
    private val members: Members,
    private val line: Line,
    private val mode: Mode,
    private val pendingSteps: PendingSteps,
    private val awaitedStep: Step?,
    private val movedBattleUnits: MovedBattleUnits,
    private val talkingNpcId: NpcId?,
    private val events: Set<PartyEvent>,
) {
    companion object {
        /** Battle units: the party, the leader first. Units: the unit of each battle unit, in the same order. */
        fun start(
            playerId: String,
            battleUnitIds: List<String>,
            unitIds: List<String>,
        ): Party {
            if (battleUnitIds.isEmpty()) throw PartyRequired()
            return Party(
                playerId = PlayerId(playerId),
                members = Members(battleUnitIds),
                line = Line(battleUnitIds),
                mode = Mode.EXPLORING,
                pendingSteps = PendingSteps(emptyList()),
                awaitedStep = null,
                movedBattleUnits = MovedBattleUnits(emptySet()),
                talkingNpcId = null,
                events = setOf(ExplorationStarted(playerId = playerId, battleUnitIds = battleUnitIds, unitIds = unitIds)),
            )
        }
    }

    fun toDto() =
        Dto(
            playerId = playerId.value,
            leaderBattleUnitId = members.value.firstOrNull(),
            battleUnitIds = members.value,
            lineBattleUnitIds = walkingLine(),
            isExploring = mode == Mode.EXPLORING,
            isWalking = isWalking(),
            talkingNpcId = talkingNpcId?.value,
            isInFormation = mode == Mode.FORMATION,
        )

    /** Whether the party can walk: it is exploring and has finished walking. */
    fun canWalk(): Boolean = mode == Mode.EXPLORING && !isWalking()

    /** Steps: where each battle unit of the party walks to, in walking order (the leader first). */
    fun walkParty(steps: List<Dto.StepDto>): Party {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isWalking()) throw AlreadyWalking()
        if (steps.isEmpty()) throw StepsRequired()
        return copy(pendingSteps = PendingSteps(steps.map(::Step)))
    }

    /**
     * Swaps the tiles of the leader and the follower on its next tile, as its single step, and rebuilds the line from the
     * positions after the swap: from the leader, the closest remaining member by tile distance follows next, ties broken
     * by party order. Positions: where each battle unit of the party stands before the swap.
     */
    fun swapLeader(
        followerId: String,
        positions: Map<String, Dto.PositionDto>,
    ): Party {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isWalking()) throw AlreadyWalking()
        val leaderId = members.value.first()
        val leaderPosition = positions[leaderId]
        val followerPosition = positions[followerId]
        if (followerId == leaderId || followerId !in members.value || leaderPosition == null || followerPosition == null) throw NotAFollower()
        val swappedPositions = positions + (leaderId to followerPosition) + (followerId to leaderPosition)
        return copy(
            line = Line.closestFirst(leaderId = leaderId, memberIds = members.value, positions = swappedPositions),
            awaitedStep = Step(Dto.StepDto(battleUnitId = leaderId, row = followerPosition.row, column = followerPosition.column)),
        )
    }

    /** The next step to walk, or `null` while awaiting the previous one or when the party has finished walking. */
    fun nextStep(): Dto.StepDto? = if (awaitedStep == null) pendingSteps.value.firstOrNull()?.toDto() else null

    /** Awaits the next step, once its walk is requested. */
    fun awaitNextStep(): Party {
        val step = pendingSteps.value.first()
        return copy(awaitedStep = step, pendingSteps = PendingSteps(pendingSteps.value.drop(1)))
    }

    /** Skips the next step, e.g.: when its battle unit can not reach its tile. */
    fun skipNextStep(): Party = copy(pendingSteps = PendingSteps(pendingSteps.value.drop(1))).finishWalkWhenDone()

    /** Resolves the awaited step when the battle unit occupies its tile. Otherwise, nothing changes. */
    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Party {
        if (awaitedStep != Step(Dto.StepDto(battleUnitId = battleUnitId, row = row, column = column))) return this
        return copy(awaitedStep = null).finishWalkWhenDone()
    }

    /** Talks to the npc, only when the party can walk (exploring, and done walking). Otherwise, nothing changes. */
    fun talk(npcId: String): Party {
        if (!canWalk()) return this
        return copy(
            mode = Mode.TALKING,
            talkingNpcId = NpcId(npcId),
            events = events + NpcTalkStarted(playerId = playerId.value, npcId = npcId),
        )
    }

    /** Explores again once the talk is finished. Nothing changes when it is not talking. */
    fun finishTalk(): Party {
        val npcId = talkingNpcId ?: return this
        return copy(
            mode = Mode.EXPLORING,
            talkingNpcId = null,
            events = events + NpcTalkFinished(playerId = playerId.value, npcId = npcId.value),
        )
    }

    /** Opens the formation, only when the party can walk (exploring, and done walking). Otherwise, nothing changes. */
    fun openFormation(): Party {
        if (!canWalk()) return this
        return copy(mode = Mode.FORMATION, events = events + FormationOpened(playerId = playerId.value))
    }

    /**
     * Closes the formation with the new party order, the leader first, so the party explores again. Nobody moves: the
     * line is rebuilt from the new leader, as after a swap, unless the order is the same. Positions: where each battle
     * unit of the party stands. Nothing changes when it is not in formation.
     */
    fun closeFormation(
        battleUnitIds: List<String>,
        positions: Map<String, Dto.PositionDto>,
    ): Party {
        if (mode != Mode.FORMATION) return this
        if (battleUnitIds.size != members.value.size || battleUnitIds.toSet() != members.value.toSet()) throw NotTheParty()
        val reorderedLine =
            when {
                battleUnitIds == members.value -> line
                battleUnitIds.first() !in positions -> Line(battleUnitIds)
                else -> Line.closestFirst(leaderId = battleUnitIds.first(), memberIds = battleUnitIds, positions = positions)
            }
        return copy(
            mode = Mode.EXPLORING,
            members = Members(battleUnitIds),
            line = reorderedLine,
            events = events + FormationClosed(playerId = playerId.value, battleUnitIds = battleUnitIds),
        )
    }

    /** Stops exploring, e.g.: when a battle starts. A walk, a talk or a formation in progress is abandoned. */
    fun pause(): Party =
        copy(
            mode = Mode.BATTLING,
            pendingSteps = PendingSteps(emptyList()),
            awaitedStep = null,
            movedBattleUnits = MovedBattleUnits(emptySet()),
            talkingNpcId = null,
            events = events + ExplorationPaused(playerId = playerId.value),
        )

    /** Explores again, e.g.: when a battle finishes. */
    fun resume(): Party =
        copy(
            mode = Mode.EXPLORING,
            line = Line(members.value),
            events = events + ExplorationResumed(playerId = playerId.value, battleUnitIds = members.value),
        )

    /** Adds the battle unit of the unit to the party, last, e.g.: a recruited npc. Nothing changes when it is already in it. */
    fun join(
        battleUnitId: String,
        unitId: String,
    ): Party {
        if (battleUnitId in members.value) return this
        return copy(
            members = Members(members.value + battleUnitId),
            line = Line(line.value + battleUnitId),
            events = events + PartyJoined(playerId = playerId.value, battleUnitId = battleUnitId, unitId = unitId),
        )
    }

    /** Removes the battle unit from the party, e.g.: when it is defeated. The next one leads when it was leading. */
    fun leave(battleUnitId: String): Party = copy(members = Members(members.value - battleUnitId), line = Line(line.value - battleUnitId))

    /** Remembers that the battle unit moved during the current second, any battle unit (e.g.: a CPU one). Only while exploring. */
    fun markMoved(battleUnitId: String): Party {
        if (mode != Mode.EXPLORING) return this
        return copy(movedBattleUnits = MovedBattleUnits(movedBattleUnits.value + battleUnitId))
    }

    /** The given battle units (e.g.: the battlefield occupants) that did not move during the current second. */
    fun standingBattleUnitIds(battleUnitIds: Set<String>): Set<String> = battleUnitIds - movedBattleUnits.value

    /** Starts the next second, in which no battle unit has moved yet. */
    fun startNextSecond(): Party =
        copy(
            movedBattleUnits = MovedBattleUnits(emptySet()),
            events = events + ExplorationSecondStarted(playerId = playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

    // The leader first, as the line may have it elsewhere once another member leads (e.g.: the leader was defeated).
    private fun walkingLine(): List<String> {
        val leaderId = members.value.firstOrNull() ?: return emptyList()
        return listOf(leaderId) + (line.value - leaderId)
    }

    private fun isWalking(): Boolean = awaitedStep != null || pendingSteps.value.isNotEmpty()

    private fun finishWalkWhenDone(): Party {
        if (isWalking()) return this
        return copy(events = events + PartyWalked(playerId = playerId.value, battleUnitIds = members.value))
    }

    private enum class Mode {
        EXPLORING,
        TALKING,
        FORMATION,
        BATTLING,
    }

    @JvmInline private value class PlayerId(
        val value: String,
    )

    @JvmInline private value class NpcId(
        val value: String,
    )

    @JvmInline private value class Members(
        val value: List<String>,
    )

    @JvmInline private value class Line(
        val value: List<String>,
    ) {
        companion object {
            fun closestFirst(
                leaderId: String,
                memberIds: List<String>,
                positions: Map<String, Dto.PositionDto>,
            ): Line {
                val lined = mutableListOf(leaderId)
                val remaining = (memberIds - leaderId).filter { memberId -> memberId in positions }.toMutableList()
                while (remaining.isNotEmpty()) {
                    val ahead = positions.getValue(lined.last())
                    val closest = remaining.minBy { memberId -> distance(positions.getValue(memberId), ahead) }
                    lined += closest
                    remaining -= closest
                }
                return Line(lined + (memberIds - lined.toSet()))
            }

            // The tiles between both, in rows plus columns.
            private fun distance(
                position: Dto.PositionDto,
                other: Dto.PositionDto,
            ): Int = abs(position.row - other.row) + abs(position.column - other.column)
        }
    }

    @JvmInline private value class PendingSteps(
        val value: List<Step>,
    )

    @JvmInline private value class MovedBattleUnits(
        val value: Set<String>,
    )

    @JvmInline private value class Step(
        val value: Dto.StepDto,
    ) {
        fun toDto() = value
    }

    /**
     * Leader: `null` once every battle unit of the party has been defeated. Line: the battle units in walking order, the
     * leader first. Exploring: `false` while battling, while talking and while in formation. Talking npc: the npc the
     * party talks to, or `null` when it talks to none. In formation: whether the player sets the party order.
     */
    data class Dto(
        val playerId: String,
        val leaderBattleUnitId: String?,
        val battleUnitIds: List<String>,
        val lineBattleUnitIds: List<String>,
        val isExploring: Boolean,
        val isWalking: Boolean,
        val talkingNpcId: String?,
        val isInFormation: Boolean,
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

        /** A tile of the battlefield. */
        data class PositionDto(
            val row: Int,
            val column: Int,
        )

        /** A battle unit walking to a tile. */
        data class StepDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )
    }
}
