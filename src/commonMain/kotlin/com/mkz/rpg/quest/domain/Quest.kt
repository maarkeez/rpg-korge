package com.mkz.rpg.quest.domain

import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.RewardDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.domain.QuestError.EmptyQuestReward
import com.mkz.rpg.quest.domain.QuestError.EmptyTaskTiles
import com.mkz.rpg.quest.domain.QuestError.InvalidTask
import com.mkz.rpg.quest.domain.QuestError.MainQuestRepeatable
import com.mkz.rpg.quest.domain.QuestError.MainQuestWithGiver
import com.mkz.rpg.quest.domain.QuestError.NegativeQuestRewardGold
import com.mkz.rpg.quest.domain.QuestError.QuestWithoutTasks
import com.mkz.rpg.quest.domain.QuestError.SideQuestWithoutGiver
import com.mkz.rpg.quest.domain.QuestError.TaskCountBelowOne
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import kotlin.jvm.JvmInline

/**
 * A line of tasks, completed one after the other, ending in a reward for the party leader. The game has one main quest, which runs once from a
 * new game, and any number of side quests, each started by talking to its quest giver.
 */
@ConsistentCopyVisibility
data class Quest private constructor(
    private val id: Id,
    private val type: QuestTypeDto,
    private val giverNpcId: GiverNpcId?,
    private val repeatable: Boolean,
    private val tasks: Tasks,
    private val reward: Reward,
    private val progress: Progress,
    private val events: Set<QuestEvent>,
) {
    companion object {
        /** Creates a quest that has not started, on its first task. Fails when it could never run: e.g.: it has no tasks, or pays nothing. */
        fun create(dto: Dto): Quest {
            if (dto.type == QuestTypeDto.MAIN && dto.giverNpcId != null) throw MainQuestWithGiver()
            if (dto.type == QuestTypeDto.MAIN && dto.repeatable) throw MainQuestRepeatable()
            if (dto.type == QuestTypeDto.SIDE && dto.giverNpcId == null) throw SideQuestWithoutGiver()
            return Quest(
                id = Id(dto.id),
                type = dto.type,
                giverNpcId = dto.giverNpcId?.let(::GiverNpcId),
                repeatable = dto.repeatable,
                tasks = Tasks(dto.tasks.map(Task::create)),
                reward = Reward(gold = dto.reward.gold, lootableItemIds = dto.reward.lootableItemIds),
                progress = Progress(status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskProgress = 0),
                events = emptySet(),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    /** Starts it from its first task. */
    fun start(): Quest =
        copy(
            progress = Progress(status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskProgress = 0),
            events = events + QuestStarted(questId = id.value),
        )

    fun toDto() =
        Dto(
            id = id.value,
            type = type,
            giverNpcId = giverNpcId?.value,
            repeatable = repeatable,
            tasks = tasks.value.map(Task::toDto),
            reward = RewardDto(gold = reward.gold, lootableItemIds = reward.lootableItemIds),
            progress =
                ProgressDto(
                    questId = id.value,
                    status = progress.status,
                    currentTaskIndex = progress.currentTaskIndex,
                    currentTaskProgress = progress.currentTaskProgress,
                ),
        )

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class GiverNpcId(
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
                    TaskDto.TypeDto.DEFEAT_UNITS -> (dto.defeatUnits ?: throw InvalidTask()).let { task -> DefeatUnits(task.unitId, task.count) }
                    TaskDto.TypeDto.TALK_TO_NPC -> TalkToNpc((dto.talkToNpc ?: throw InvalidTask()).npcId)
                    TaskDto.TypeDto.REACH_TILES -> ReachTiles((dto.reachTiles ?: throw InvalidTask()).tiles)
                }
        }

        fun toDto(): TaskDto

        data class DefeatUnits(
            val unitId: String,
            val count: Int,
        ) : Task {
            init {
                if (count < 1) throw TaskCountBelowOne()
            }

            override fun toDto() = TaskDto.defeatUnits(unitId = unitId, count = count)
        }

        data class TalkToNpc(
            val npcId: String,
        ) : Task {
            override fun toDto() = TaskDto.talkToNpc(npcId = npcId)
        }

        data class ReachTiles(
            val tiles: List<PositionDto>,
        ) : Task {
            init {
                if (tiles.isEmpty()) throw EmptyTaskTiles()
            }

            override fun toDto() = TaskDto.reachTiles(tiles = tiles)
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
        val currentTaskProgress: Int,
    )

    /** Progress: where it is in its life, which the save keeps. The rest is read from the resources. */
    data class Dto(
        val id: String,
        val type: QuestTypeDto,
        val giverNpcId: String?,
        val repeatable: Boolean,
        val tasks: List<TaskDto>,
        val reward: RewardDto,
        val progress: ProgressDto,
    ) {
        enum class QuestTypeDto {
            MAIN,
            SIDE,
        }

        data class TaskDto(
            val type: TypeDto,
            val defeatUnits: DefeatUnitsDto?,
            val talkToNpc: TalkToNpcDto?,
            val reachTiles: ReachTilesDto?,
        ) {
            companion object {
                fun defeatUnits(
                    unitId: String,
                    count: Int,
                ) = TaskDto(type = TypeDto.DEFEAT_UNITS, defeatUnits = DefeatUnitsDto(unitId = unitId, count = count), talkToNpc = null, reachTiles = null)

                fun talkToNpc(npcId: String) = TaskDto(type = TypeDto.TALK_TO_NPC, defeatUnits = null, talkToNpc = TalkToNpcDto(npcId = npcId), reachTiles = null)

                fun reachTiles(tiles: List<PositionDto>) = TaskDto(type = TypeDto.REACH_TILES, defeatUnits = null, talkToNpc = null, reachTiles = ReachTilesDto(tiles = tiles))
            }

            enum class TypeDto {
                DEFEAT_UNITS,
                TALK_TO_NPC,
                REACH_TILES,
            }

            /** Defeat [count] battle units of the unit, of any player but the party's. */
            data class DefeatUnitsDto(
                val unitId: String,
                val count: Int,
            )

            /** Finish talking to the npc. */
            data class TalkToNpcDto(
                val npcId: String,
            )

            /** Touch any of the tiles with any party member. */
            data class ReachTilesDto(
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
            val currentTaskProgress: Int,
        ) {
            companion object {
                fun notStarted(questId: String) = ProgressDto(questId = questId, status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskProgress = 0)
            }
        }
    }
}
