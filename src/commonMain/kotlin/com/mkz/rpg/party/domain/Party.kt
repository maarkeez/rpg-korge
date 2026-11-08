package com.mkz.rpg.party.domain

import com.mkz.rpg.party.domain.PartyError.AlreadyStepping
import com.mkz.rpg.party.domain.PartyError.NotAFollower
import com.mkz.rpg.party.domain.PartyError.NotExploring
import com.mkz.rpg.party.domain.PartyError.NotTheParty
import com.mkz.rpg.party.domain.PartyError.PartyRequired
import com.mkz.rpg.party.domain.PartyError.StepsRequired
import com.mkz.rpg.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationSecondStarted
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.party.domain.PartyEvent.FormationClosed
import com.mkz.rpg.party.domain.PartyEvent.FormationOpened
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkStarted
import com.mkz.rpg.party.domain.PartyEvent.PartyJoined
import com.mkz.rpg.party.domain.PartyEvent.PartyStepped
import com.mkz.rpg.party.domain.PartyEvent.PauseClosed
import com.mkz.rpg.party.domain.PartyEvent.PauseOpened
import com.mkz.rpg.party.domain.PartyEvent.QuestClosed
import com.mkz.rpg.party.domain.PartyEvent.QuestOpened
import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * The human player exploring the battlefield with their party, out of the battles: the leader steps where the player
 * says, and the rest follow it in walking order.
 *
 * Each party step is one battle unit's step at a time: each step is awaited until its battle unit occupies its tile, so
 * the next battle unit can step on the tile it left.
 *
 * The followers walk in walking order, which is kept apart from the party order: the leader swaps tiles with a party
 * member on its next tile, and the walking order is rebuilt then, so each follower follows the closest member ahead of
 * it.
 *
 * Every second while exploring, the battle units that stood still during it receive the effect of their terrain, so it
 * remembers the battle units that moved during the current second.
 *
 * The party can talk to an npc while exploring. Time stops while talking: the party does not step, and no second starts.
 * Time also stops while the player sets the party order in the formation, which decides who leads, while the game is
 * paused (e.g.: to save it), and while the player reads a quest.
 */
@ConsistentCopyVisibility
data class Party private constructor(
    private val playerId: PlayerId,
    private val members: Members,
    private val walkingOrder: WalkingOrder,
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
                walkingOrder = WalkingOrder(battleUnitIds),
                mode = Mode.EXPLORING,
                pendingSteps = PendingSteps(emptyList()),
                awaitedStep = null,
                movedBattleUnits = MovedBattleUnits(emptySet()),
                talkingNpcId = null,
                events = setOf(ExplorationStarted(playerId = playerId, battleUnitIds = battleUnitIds, unitIds = unitIds)),
            )
        }

        /**
         * Rebuilds the party as it was when the game was saved, from its [toSnapshot], exploring, done stepping, and with
         * no battle unit moved in the current second. It publishes no events.
         */
        fun restore(snapshot: Dto.SnapshotDto): Party {
            if (snapshot.battleUnitIds.isEmpty()) throw PartyRequired()
            return Party(
                playerId = PlayerId(snapshot.playerId),
                members = Members(snapshot.battleUnitIds),
                walkingOrder = WalkingOrder(snapshot.walkingOrderBattleUnitIds),
                mode = Mode.EXPLORING,
                pendingSteps = PendingSteps(emptyList()),
                awaitedStep = null,
                movedBattleUnits = MovedBattleUnits(emptySet()),
                talkingNpcId = null,
                events = emptySet(),
            )
        }
    }

    fun toDto() =
        Dto(
            playerId = playerId.value,
            leaderBattleUnitId = members.value.firstOrNull(),
            battleUnitIds = members.value,
            walkingOrderBattleUnitIds = leaderFirstWalkingOrder(),
            isExploring = mode == Mode.EXPLORING,
            isStepping = isStepping(),
            talkingNpcId = talkingNpcId?.value,
            isInFormation = mode == Mode.FORMATION,
            isPaused = mode == Mode.PAUSED,
            isReadingQuest = mode == Mode.READING_QUEST,
        )

    /** What is kept when the game is saved: the members and the walking order, as [restore] explores again. */
    fun toSnapshot() = Dto.SnapshotDto(playerId = playerId.value, battleUnitIds = members.value, walkingOrderBattleUnitIds = walkingOrder.value)

    /** Whether the party can step: it is exploring and its party step is done. */
    fun canStep(): Boolean = mode == Mode.EXPLORING && !isStepping()

    /** Plans a party step. Steps: where each battle unit of the party walks to, in walking order (the leader first). */
    fun stepParty(steps: List<Dto.StepDto>): Party {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isStepping()) throw AlreadyStepping()
        if (steps.isEmpty()) throw StepsRequired()
        return copy(pendingSteps = PendingSteps(steps.map(::Step)))
    }

    /**
     * Swaps the tiles of the leader and the follower on its next tile, as its single step, and rebuilds the walking order
     * from the positions after the swap: from the leader, the closest remaining member by tile distance follows next, ties
     * broken by party order. Positions: where each battle unit of the party stands before the swap.
     */
    fun swapLeader(
        followerId: String,
        positions: Map<String, Dto.PositionDto>,
    ): Party {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isStepping()) throw AlreadyStepping()
        val leaderId = members.value.first()
        val leaderPosition = positions[leaderId]
        val followerPosition = positions[followerId]
        if (followerId == leaderId || followerId !in members.value || leaderPosition == null || followerPosition == null) throw NotAFollower()
        val swappedPositions = positions + (leaderId to followerPosition) + (followerId to leaderPosition)
        return copy(
            walkingOrder = WalkingOrder.closestFirst(leaderId = leaderId, memberIds = members.value, positions = swappedPositions),
            awaitedStep = Step(Dto.StepDto(battleUnitId = leaderId, row = followerPosition.row, column = followerPosition.column)),
        )
    }

    /** The next step to walk, or `null` while awaiting the previous one or when the party step is done. */
    fun nextStep(): Dto.StepDto? = if (awaitedStep == null) pendingSteps.value.firstOrNull()?.toDto() else null

    /** Awaits the next step, once its walk is requested. */
    fun awaitNextStep(): Party {
        val step = pendingSteps.value.first()
        return copy(awaitedStep = step, pendingSteps = PendingSteps(pendingSteps.value.drop(1)))
    }

    /** Skips the next step, e.g.: when its battle unit can not reach its tile. */
    fun skipNextStep(): Party = copy(pendingSteps = PendingSteps(pendingSteps.value.drop(1))).finishStepWhenDone()

    /** Resolves the awaited step when the battle unit occupies its tile. Otherwise, nothing changes. */
    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Party {
        if (awaitedStep != Step(Dto.StepDto(battleUnitId = battleUnitId, row = row, column = column))) return this
        return copy(awaitedStep = null).finishStepWhenDone()
    }

    /** Talks to the npc, only when the party can step (exploring, and done stepping). Otherwise, nothing changes. */
    fun talk(npcId: String): Party {
        if (!canStep()) return this
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

    /** Opens the formation, only when the party can step (exploring, and done stepping). Otherwise, nothing changes. */
    fun openFormation(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.FORMATION, events = events + FormationOpened(playerId = playerId.value))
    }

    /**
     * Closes the formation with the new party order, the leader first, so the party explores again. Nobody moves: the
     * walking order is rebuilt from the new leader, as after a swap, unless the order is the same. Positions: where each
     * battle unit of the party stands. Nothing changes when it is not in formation.
     */
    fun closeFormation(
        battleUnitIds: List<String>,
        positions: Map<String, Dto.PositionDto>,
    ): Party {
        if (mode != Mode.FORMATION) return this
        if (battleUnitIds.size != members.value.size || battleUnitIds.toSet() != members.value.toSet()) throw NotTheParty()
        val reorderedWalkingOrder =
            when {
                battleUnitIds == members.value -> walkingOrder
                battleUnitIds.first() !in positions -> WalkingOrder(battleUnitIds)
                else -> WalkingOrder.closestFirst(leaderId = battleUnitIds.first(), memberIds = battleUnitIds, positions = positions)
            }
        return copy(
            mode = Mode.EXPLORING,
            members = Members(battleUnitIds),
            walkingOrder = reorderedWalkingOrder,
            events = events + FormationClosed(playerId = playerId.value, battleUnitIds = battleUnitIds),
        )
    }

    /** Pauses the game, only when the party can step (exploring, and done stepping). Otherwise, nothing changes. */
    fun openPause(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.PAUSED, events = events + PauseOpened(playerId = playerId.value))
    }

    /** Explores again once the pause is closed (e.g.: Resume). Nothing changes when it is not paused. */
    fun closePause(): Party {
        if (mode != Mode.PAUSED) return this
        return copy(mode = Mode.EXPLORING, events = events + PauseClosed(playerId = playerId.value))
    }

    /** Reads a quest, only when the party can step (exploring, and done stepping). Otherwise, nothing changes. */
    fun openQuest(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.READING_QUEST, events = events + QuestOpened(playerId = playerId.value))
    }

    /** Explores again once the quest is closed (e.g.: Back). Nothing changes when it is not reading. */
    fun closeQuest(): Party {
        if (mode != Mode.READING_QUEST) return this
        return copy(mode = Mode.EXPLORING, events = events + QuestClosed(playerId = playerId.value))
    }

    /** Interrupts the exploration when a battle starts: a party step, a talk, a formation or a pause in progress is abandoned. */
    fun interrupt(): Party =
        copy(
            mode = Mode.BATTLING,
            pendingSteps = PendingSteps(emptyList()),
            awaitedStep = null,
            movedBattleUnits = MovedBattleUnits(emptySet()),
            talkingNpcId = null,
            events = events + ExplorationInterrupted(playerId = playerId.value),
        )

    /** Resumes the exploration when the battle that interrupted it finishes. */
    fun resume(): Party =
        copy(
            mode = Mode.EXPLORING,
            walkingOrder = WalkingOrder(members.value),
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
            walkingOrder = WalkingOrder(walkingOrder.value + battleUnitId),
            events = events + PartyJoined(playerId = playerId.value, battleUnitId = battleUnitId, unitId = unitId),
        )
    }

    /** Removes the battle unit from the party, e.g.: when it is defeated. The next one leads when it was leading. */
    fun leave(battleUnitId: String): Party = copy(members = Members(members.value - battleUnitId), walkingOrder = WalkingOrder(walkingOrder.value - battleUnitId))

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

    // The leader first, as the walking order may have it elsewhere once another member leads (e.g.: the leader was defeated).
    private fun leaderFirstWalkingOrder(): List<String> {
        val leaderId = members.value.firstOrNull() ?: return emptyList()
        return listOf(leaderId) + (walkingOrder.value - leaderId)
    }

    private fun isStepping(): Boolean = awaitedStep != null || pendingSteps.value.isNotEmpty()

    private fun finishStepWhenDone(): Party {
        if (isStepping()) return this
        return copy(events = events + PartyStepped(playerId = playerId.value, battleUnitIds = members.value))
    }

    private enum class Mode {
        EXPLORING,
        TALKING,
        FORMATION,
        PAUSED,
        READING_QUEST,
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

    @JvmInline private value class WalkingOrder(
        val value: List<String>,
    ) {
        companion object {
            fun closestFirst(
                leaderId: String,
                memberIds: List<String>,
                positions: Map<String, Dto.PositionDto>,
            ): WalkingOrder {
                val lined = mutableListOf(leaderId)
                val remaining = (memberIds - leaderId).filter { memberId -> memberId in positions }.toMutableList()
                while (remaining.isNotEmpty()) {
                    val ahead = positions.getValue(lined.last())
                    val closest = remaining.minBy { memberId -> distance(positions.getValue(memberId), ahead) }
                    lined += closest
                    remaining -= closest
                }
                return WalkingOrder(lined + (memberIds - lined.toSet()))
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
     * Leader: `null` once every battle unit of the party has been defeated. Walking order: the battle units in walking
     * order, the leader first. Exploring: `false` while battling, while talking and while in formation. Talking npc: the npc the
     * party talks to, or `null` when it talks to none. In formation: whether the player sets the party order. Paused:
     * whether the game is paused, e.g.: to save it. Reading quest: whether the player reads a quest.
     */
    data class Dto(
        val playerId: String,
        val leaderBattleUnitId: String?,
        val battleUnitIds: List<String>,
        val walkingOrderBattleUnitIds: List<String>,
        val isExploring: Boolean,
        val isStepping: Boolean,
        val talkingNpcId: String?,
        val isInFormation: Boolean,
        val isPaused: Boolean,
        val isReadingQuest: Boolean,
    ) {
        /** Where the leader steps to, one tile away. */
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

        /** The party as the game was saved. Battle units: the party, the leader first. Walking order: as kept. */
        data class SnapshotDto(
            val playerId: String,
            val battleUnitIds: List<String>,
            val walkingOrderBattleUnitIds: List<String>,
        )
    }
}
