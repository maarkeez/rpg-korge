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

        fun load(snapshot: Dto.SnapshotDto): Party {
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

    fun toSnapshot() = Dto.SnapshotDto(playerId = playerId.value, battleUnitIds = members.value, walkingOrderBattleUnitIds = walkingOrder.value)

    fun canStep(): Boolean = mode == Mode.EXPLORING && !isStepping()

    fun stepParty(steps: List<Dto.StepDto>): Party {
        if (mode != Mode.EXPLORING) throw NotExploring()
        if (isStepping()) throw AlreadyStepping()
        if (steps.isEmpty()) throw StepsRequired()
        return copy(pendingSteps = PendingSteps(steps.map(::Step)))
    }

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

    fun nextStep(): Dto.StepDto? = if (awaitedStep == null) pendingSteps.value.firstOrNull()?.toDto() else null

    fun awaitNextStep(): Party {
        val step = pendingSteps.value.first()
        return copy(awaitedStep = step, pendingSteps = PendingSteps(pendingSteps.value.drop(1)))
    }

    fun skipNextStep(): Party = copy(pendingSteps = PendingSteps(pendingSteps.value.drop(1))).finishStepWhenDone()

    fun resolveStep(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Party {
        if (awaitedStep != Step(Dto.StepDto(battleUnitId = battleUnitId, row = row, column = column))) return this
        return copy(awaitedStep = null).finishStepWhenDone()
    }

    fun talk(npcId: String): Party {
        if (!canStep()) return this
        return copy(
            mode = Mode.TALKING,
            talkingNpcId = NpcId(npcId),
            events = events + NpcTalkStarted(playerId = playerId.value, npcId = npcId),
        )
    }

    fun finishTalk(): Party {
        val npcId = talkingNpcId ?: return this
        return copy(
            mode = Mode.EXPLORING,
            talkingNpcId = null,
            events = events + NpcTalkFinished(playerId = playerId.value, npcId = npcId.value),
        )
    }

    fun openFormation(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.FORMATION, events = events + FormationOpened(playerId = playerId.value))
    }

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

    fun openPause(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.PAUSED, events = events + PauseOpened(playerId = playerId.value))
    }

    fun closePause(): Party {
        if (mode != Mode.PAUSED) return this
        return copy(mode = Mode.EXPLORING, events = events + PauseClosed(playerId = playerId.value))
    }

    fun openQuest(): Party {
        if (!canStep()) return this
        return copy(mode = Mode.READING_QUEST, events = events + QuestOpened(playerId = playerId.value))
    }

    fun closeQuest(): Party {
        if (mode != Mode.READING_QUEST) return this
        return copy(mode = Mode.EXPLORING, events = events + QuestClosed(playerId = playerId.value))
    }

    fun interrupt(): Party =
        copy(
            mode = Mode.BATTLING,
            pendingSteps = PendingSteps(emptyList()),
            awaitedStep = null,
            movedBattleUnits = MovedBattleUnits(emptySet()),
            talkingNpcId = null,
            events = events + ExplorationInterrupted(playerId = playerId.value),
        )

    fun resume(): Party =
        copy(
            mode = Mode.EXPLORING,
            walkingOrder = WalkingOrder(members.value),
            events = events + ExplorationResumed(playerId = playerId.value, battleUnitIds = members.value),
        )

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

    fun leave(battleUnitId: String): Party = copy(members = Members(members.value - battleUnitId), walkingOrder = WalkingOrder(walkingOrder.value - battleUnitId))

    fun markMoved(battleUnitId: String): Party {
        if (mode != Mode.EXPLORING) return this
        return copy(movedBattleUnits = MovedBattleUnits(movedBattleUnits.value + battleUnitId))
    }

    fun standingBattleUnitIds(battleUnitIds: Set<String>): Set<String> = battleUnitIds - movedBattleUnits.value

    fun startNextSecond(): Party =
        copy(
            movedBattleUnits = MovedBattleUnits(emptySet()),
            events = events + ExplorationSecondStarted(playerId = playerId.value),
        )

    fun pullEvents() = events to copy(events = emptySet())

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
                    val closest = remaining.minBy { memberId -> rowsPlusColumnsBetween(positions.getValue(memberId), ahead) }
                    lined += closest
                    remaining -= closest
                }
                return WalkingOrder(lined + (memberIds - lined.toSet()))
            }

            private fun rowsPlusColumnsBetween(
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
        enum class DirectionDto(
            val rowOffset: Int,
            val columnOffset: Int,
        ) {
            NORTH(rowOffset = -1, columnOffset = 0),
            EAST(rowOffset = 0, columnOffset = 1),
            SOUTH(rowOffset = 1, columnOffset = 0),
            WEST(rowOffset = 0, columnOffset = -1),
        }

        data class PositionDto(
            val row: Int,
            val column: Int,
        )

        data class StepDto(
            val battleUnitId: String,
            val row: Int,
            val column: Int,
        )

        data class SnapshotDto(
            val playerId: String,
            val battleUnitIds: List<String>,
            val walkingOrderBattleUnitIds: List<String>,
        )
    }
}
