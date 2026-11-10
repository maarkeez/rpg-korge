package com.mkz.rpg.quest.adapters.resources

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.RewardDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.usecases.services.QuestLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one quest per `quest/<quest id>.toml` resource. */
class ResourcesQuestLoader(
    private val questRoot: VfsFile = ResourcesBundle.root["quest"],
) : QuestLoader {
    private var quests = emptyList<Quest.Dto>()

    override suspend fun initResources() {
        quests =
            questRoot.readTomlFiles<QuestDefinition>().map { (questId, definition) ->
                fromResource("quest/$questId.toml") { definition.toDto(questId) }
            }
    }

    override fun loadQuests(): List<Quest.Dto> = quests

    @Serializable
    data class QuestDefinition(
        val type: String,
        val questGiverNpcId: String? = null,
        val startTiles: List<List<Int>> = emptyList(),
        val repeatable: Boolean = false,
        val tasks: List<TaskDefinition> = emptyList(),
        val reward: RewardDefinition,
    ) {
        fun toDto(questId: String): Quest.Dto =
            Quest.Dto(
                id = questId,
                type = enumOf<QuestTypeDto>("type", type),
                questGiverNpcId = questGiverNpcId,
                startTiles = startTiles.map { tile -> positionOf("startTiles", tile) },
                repeatable = repeatable,
                tasks = tasks.map(TaskDefinition::toDto),
                reward = reward.toDto(),
                progress = ProgressDto.notStarted(questId),
            )
    }

    @Serializable
    data class TaskDefinition(
        val type: String,
        val unitId: String? = null,
        val requiredDefeats: Int? = null,
        val npcId: String? = null,
        val tiles: List<List<Int>>? = null,
    ) {
        fun toDto(): TaskDto =
            when (enumOf<TaskDto.TypeDto>("tasks.type", type)) {
                TaskDto.TypeDto.DEFEAT -> TaskDto.defeat(unitId = required("tasks.unitId", unitId), requiredDefeats = required("tasks.requiredDefeats", requiredDefeats))
                TaskDto.TypeDto.TALK -> TaskDto.talk(npcId = required("tasks.npcId", npcId))
                TaskDto.TypeDto.VISIT ->
                    TaskDto.visit(
                        tiles =
                            required("tasks.tiles", tiles).map { tile -> positionOf("tasks.tiles", tile) },
                    )
            }
    }

    @Serializable
    data class RewardDefinition(
        val gold: Int = 0,
        val lootableItemIds: List<String> = emptyList(),
    ) {
        fun toDto(): RewardDto = RewardDto(gold = gold, lootableItemIds = lootableItemIds)
    }

    private companion object {
        fun positionOf(
            field: String,
            tile: List<Int>,
        ): PositionDto {
            require(tile.size == 2) { "'$field' must hold lists of [row, column], but held $tile" }
            return PositionDto(row = tile[0], column = tile[1])
        }
    }
}
