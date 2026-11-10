package com.mkz.rpg.quest.domain

import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.RewardDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import korlibs.io.util.UUID

object QuestMother {
    fun mainQuest(
        id: String = id(),
        tasks: List<TaskDto> = listOf(defeat()),
        reward: RewardDto = reward(),
        startTiles: List<PositionDto> = listOf(PositionDto(row = 0, column = 0)),
    ): Quest =
        Quest.create(
            Quest.Dto(id = id, type = QuestTypeDto.MAIN, questGiverNpcId = null, startTiles = startTiles, repeatable = false, tasks = tasks, reward = reward, progress = ProgressDto.notStarted(id)),
        )

    fun sideQuest(
        id: String = id(),
        questGiverNpcId: String = "farmer",
        repeatable: Boolean = false,
        tasks: List<TaskDto> = listOf(defeat()),
        reward: RewardDto = reward(),
    ): Quest =
        Quest.create(
            Quest.Dto(
                id = id,
                type = QuestTypeDto.SIDE,
                questGiverNpcId = questGiverNpcId,
                startTiles = emptyList(),
                repeatable = repeatable,
                tasks = tasks,
                reward = reward,
                progress = ProgressDto.notStarted(id),
            ),
        )

    fun defeat(
        unitId: String = "goat-wizard",
        requiredDefeats: Int = 1,
    ) = TaskDto.defeat(unitId = unitId, requiredDefeats = requiredDefeats)

    fun talk(npcId: String = "farmer") = TaskDto.talk(npcId = npcId)

    fun visit(vararg tiles: Pair<Int, Int>) = TaskDto.visit(tiles = tiles.map { (row, column) -> PositionDto(row = row, column = column) })

    fun reward(
        gold: Int = 150,
        lootableItemIds: List<String> = emptyList(),
    ) = RewardDto(gold = gold, lootableItemIds = lootableItemIds)

    fun id() = "quest-${UUID.randomUUID()}"
}
