package com.mkz.rpg.quest.domain

import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.RewardDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.domain.QuestError.EmptyQuestReward
import com.mkz.rpg.quest.domain.QuestError.EmptyVisitTaskTiles
import com.mkz.rpg.quest.domain.QuestError.InvalidTask
import com.mkz.rpg.quest.domain.QuestError.MainQuestCanNotBeAbandoned
import com.mkz.rpg.quest.domain.QuestError.MainQuestRepeatable
import com.mkz.rpg.quest.domain.QuestError.MainQuestWithQuestGiver
import com.mkz.rpg.quest.domain.QuestError.NegativeQuestRewardGold
import com.mkz.rpg.quest.domain.QuestError.QuestNotActive
import com.mkz.rpg.quest.domain.QuestError.QuestWithoutTasks
import com.mkz.rpg.quest.domain.QuestError.RequiredDefeatsBelowOne
import com.mkz.rpg.quest.domain.QuestError.SideQuestWithoutQuestGiver
import com.mkz.rpg.quest.domain.QuestEvent.DefeatCounted
import com.mkz.rpg.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestPinned
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import kotlin.jvm.JvmInline

/**
 * A line of tasks, completed one after the other, ending in a reward granted to the party leader. The game has one main quest, which runs once from a
 * new game, and any number of side quests, each started by talking to its quest giver.
 */
@ConsistentCopyVisibility
data class Quest private constructor(
    private val id: Id,
    private val type: QuestTypeDto,
    private val questGiverNpcId: QuestGiverNpcId?,
    private val repeatable: Boolean,
    private val tasks: Tasks,
    private val reward: Reward,
    private val progress: Progress,
    private val events: Set<QuestEvent>,
) {
    companion object {
        /** Creates a quest that has not started, on its first task. Fails when it could never run: e.g.: it has no tasks, or grants nothing. */
        fun create(dto: Dto): Quest {
            if (dto.type == QuestTypeDto.MAIN && dto.questGiverNpcId != null) throw MainQuestWithQuestGiver()
            if (dto.type == QuestTypeDto.MAIN && dto.repeatable) throw MainQuestRepeatable()
            if (dto.type == QuestTypeDto.SIDE && dto.questGiverNpcId == null) throw SideQuestWithoutQuestGiver()
            return Quest(
                id = Id(dto.id),
                type = dto.type,
                questGiverNpcId = dto.questGiverNpcId?.let(::QuestGiverNpcId),
                repeatable = dto.repeatable,
                tasks = Tasks(dto.tasks.map(Task::create)),
                reward = Reward(gold = dto.reward.gold, lootableItemIds = dto.reward.lootableItemIds),
                progress = Progress(status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false),
                events = emptySet(),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    /** Whether talking to the npc starts it: it is a side quest of that quest giver, not started, or completed and repeatable. */
    fun canBeStartedBy(npcId: String): Boolean = questGiverNpcId?.value == npcId && (progress.status == StatusDto.NOT_STARTED || (progress.status == StatusDto.COMPLETED && repeatable))

    /** The sign the npc has because of this quest: a talk task when its current task is to talk to it, a quest to start when it can start it, or none. */
    fun markerFor(npcId: String): QuestMarkerDto? =
        when {
            progress.status == StatusDto.ACTIVE && (currentTask() as? Task.Talk)?.npcId == npcId -> QuestMarkerDto(npcId = npcId, sign = QuestMarkerDto.SignDto.TALK_TASK)
            canBeStartedBy(npcId) -> QuestMarkerDto(npcId = npcId, sign = QuestMarkerDto.SignDto.QUEST_TO_START)
            else -> null
        }

    /** Starts it from its first task. */
    fun start(): Quest =
        copy(
            progress = Progress(status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false),
            events = events + QuestStarted(questId = id.value),
        )

    /** Counts a defeated battle unit of the unit, when the current task asks for it. Only events from when a task became the current one count. */
    fun defeated(unitId: String): Quest {
        val task = currentTask() as? Task.Defeat
        if (progress.status != StatusDto.ACTIVE || task?.unitId != unitId) return this
        val defeats = progress.currentTaskDefeats + 1
        val progressedQuest =
            copy(
                progress = progress.copy(currentTaskDefeats = defeats),
                events = events + DefeatCounted(questId = id.value, taskIndex = progress.currentTaskIndex, defeats = defeats, requiredDefeats = task.requiredDefeats),
            )
        return if (defeats >= task.requiredDefeats) progressedQuest.completeCurrentTask() else progressedQuest
    }

    /** Completes the current task, when it is a visit task of any of the tiles. */
    fun visited(tiles: List<PositionDto>): Quest {
        val task = currentTask() as? Task.Visit
        if (progress.status != StatusDto.ACTIVE || task == null || tiles.none(task.tiles::contains)) return this
        return completeCurrentTask()
    }

    /** Completes the current task, when it asks to talk to the npc. */
    fun talkedTo(npcId: String): Quest {
        val task = currentTask() as? Task.Talk
        if (progress.status != StatusDto.ACTIVE || task?.npcId != npcId) return this
        return completeCurrentTask()
    }

    /** Goes back to not started, with its progress reset, so its quest giver can start it again even when it is not repeatable. Only an active side quest can be abandoned. */
    fun abandon(): Quest {
        if (type == QuestTypeDto.MAIN) throw MainQuestCanNotBeAbandoned()
        if (progress.status != StatusDto.ACTIVE) throw QuestNotActive()
        return copy(
            progress = Progress(status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false),
            events = events + QuestAbandoned(questId = id.value),
        )
    }

    /** Makes it the pinned quest, the one the tracker shows. Only an active quest can be pinned. */
    fun pin(): Quest {
        if (progress.status != StatusDto.ACTIVE) throw QuestNotActive()
        return copy(progress = progress.copy(isPinned = true), events = events + QuestPinned(questId = id.value))
    }

    /** It is no longer the pinned quest, e.g.: as another quest is pinned. */
    fun unpin(): Quest = copy(progress = progress.copy(isPinned = false))

    /** Moves on to the next task, or completes the quest after the last one. */
    private fun completeCurrentTask(): Quest {
        val taskCompleted = TaskCompleted(questId = id.value, taskIndex = progress.currentTaskIndex)
        return if (progress.currentTaskIndex == tasks.value.lastIndex) {
            copy(progress = progress.copy(status = StatusDto.COMPLETED, isPinned = false), events = events + taskCompleted + QuestCompleted(questId = id.value))
        } else {
            copy(progress = progress.copy(currentTaskIndex = progress.currentTaskIndex + 1, currentTaskDefeats = 0), events = events + taskCompleted)
        }
    }

    private fun currentTask(): Task = tasks.value[progress.currentTaskIndex]

    /** Puts back how far it went when the game was saved, publishing no events. */
    fun restoreProgress(progress: ProgressDto): Quest {
        require(progress.currentTaskIndex in tasks.value.indices) { "Quest ${id.value} has no task ${progress.currentTaskIndex}" }
        return copy(progress = Progress(status = progress.status, currentTaskIndex = progress.currentTaskIndex, currentTaskDefeats = progress.currentTaskDefeats, isPinned = progress.isPinned))
    }

    fun toDto() =
        Dto(
            id = id.value,
            type = type,
            questGiverNpcId = questGiverNpcId?.value,
            repeatable = repeatable,
            tasks = tasks.value.map(Task::toDto),
            reward = RewardDto(gold = reward.gold, lootableItemIds = reward.lootableItemIds),
            progress =
                ProgressDto(
                    questId = id.value,
                    status = progress.status,
                    currentTaskIndex = progress.currentTaskIndex,
                    currentTaskDefeats = progress.currentTaskDefeats,
                    isPinned = progress.isPinned,
                ),
        )

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class QuestGiverNpcId(
        val value: String,
    )

    @JvmInline private value class Tasks(
        val value: List<Task>,
    ) {
        init {
            if (value.isEmpty()) throw QuestWithoutTasks()
        }
    }

    private sealed interface Task {
        companion object {
            fun create(dto: TaskDto): Task =
                when (dto.type) {
                    TaskDto.TypeDto.DEFEAT -> (dto.defeat ?: throw InvalidTask()).let { task -> Defeat(task.unitId, task.requiredDefeats) }
                    TaskDto.TypeDto.TALK -> Talk((dto.talk ?: throw InvalidTask()).npcId)
                    TaskDto.TypeDto.VISIT -> Visit((dto.visit ?: throw InvalidTask()).tiles)
                }
        }

        fun toDto(): TaskDto

        data class Defeat(
            val unitId: String,
            val requiredDefeats: Int,
        ) : Task {
            init {
                if (requiredDefeats < 1) throw RequiredDefeatsBelowOne()
            }

            override fun toDto() = TaskDto.defeat(unitId = unitId, requiredDefeats = requiredDefeats)
        }

        data class Talk(
            val npcId: String,
        ) : Task {
            override fun toDto() = TaskDto.talk(npcId = npcId)
        }

        data class Visit(
            val tiles: List<PositionDto>,
        ) : Task {
            init {
                if (tiles.isEmpty()) throw EmptyVisitTaskTiles()
            }

            override fun toDto() = TaskDto.visit(tiles = tiles)
        }
    }

    private data class Reward(
        val gold: Int,
        val lootableItemIds: List<String>,
    ) {
        init {
            if (gold < 0) throw NegativeQuestRewardGold()
            if (gold == 0 && lootableItemIds.isEmpty()) throw EmptyQuestReward()
        }
    }

    private data class Progress(
        val status: StatusDto,
        val currentTaskIndex: Int,
        val currentTaskDefeats: Int,
        val isPinned: Boolean,
    )

    /** Progress: where it is in its life, which the save keeps. The rest is read from the resources. */
    data class Dto(
        val id: String,
        val type: QuestTypeDto,
        val questGiverNpcId: String?,
        val repeatable: Boolean,
        val tasks: List<TaskDto>,
        val reward: RewardDto,
        val progress: ProgressDto,
    ) {
        enum class QuestTypeDto {
            MAIN,
            SIDE,
        }

        /** The sign over the npc's head. */
        data class QuestMarkerDto(
            val npcId: String,
            val sign: SignDto,
        ) {
            enum class SignDto {
                QUEST_TO_START,
                TALK_TASK,
            }
        }

        data class TaskDto(
            val type: TypeDto,
            val defeat: DefeatDto?,
            val talk: TalkDto?,
            val visit: VisitDto?,
        ) {
            companion object {
                fun defeat(
                    unitId: String,
                    requiredDefeats: Int,
                ) = TaskDto(type = TypeDto.DEFEAT, defeat = DefeatDto(unitId = unitId, requiredDefeats = requiredDefeats), talk = null, visit = null)

                fun talk(npcId: String) = TaskDto(type = TypeDto.TALK, defeat = null, talk = TalkDto(npcId = npcId), visit = null)

                fun visit(tiles: List<PositionDto>) = TaskDto(type = TypeDto.VISIT, defeat = null, talk = null, visit = VisitDto(tiles = tiles))
            }

            enum class TypeDto {
                DEFEAT,
                TALK,
                VISIT,
            }

            /** Defeat [requiredDefeats] battle units of the unit, of the CPU player. */
            data class DefeatDto(
                val unitId: String,
                val requiredDefeats: Int,
            )

            /** Finish talking to the npc. */
            data class TalkDto(
                val npcId: String,
            )

            /** Walk or teleport onto any of the tiles with any party member. */
            data class VisitDto(
                val tiles: List<PositionDto>,
            )
        }

        data class PositionDto(
            val row: Int,
            val column: Int,
        )

        data class RewardDto(
            val gold: Int,
            val lootableItemIds: List<String>,
        )

        enum class StatusDto {
            NOT_STARTED,
            ACTIVE,
            COMPLETED,
        }

        data class ProgressDto(
            val questId: String,
            val status: StatusDto,
            val currentTaskIndex: Int,
            val currentTaskDefeats: Int,
            val isPinned: Boolean,
        ) {
            companion object {
                fun notStarted(questId: String) = ProgressDto(questId = questId, status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false)
            }
        }
    }
}
