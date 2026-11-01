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
        tasks: List<TaskDto> = listOf(defeatUnits()),
        reward: RewardDto = reward(),
    ): Quest = Quest.create(Quest.Dto(id = id, type = QuestTypeDto.MAIN, giverNpcId = null, repeatable = false, tasks = tasks, reward = reward, progress = ProgressDto.notStarted(id)))

    fun sideQuest(
        id: String = id(),
        giverNpcId: String = "farmer",
        repeatable: Boolean = false,
        tasks: List<TaskDto> = listOf(defeatUnits()),
        reward: RewardDto = reward(),
    ): Quest = Quest.create(Quest.Dto(id = id, type = QuestTypeDto.SIDE, giverNpcId = giverNpcId, repeatable = repeatable, tasks = tasks, reward = reward, progress = ProgressDto.notStarted(id)))

    fun defeatUnits(
        unitId: String = "goat-wizard",
        count: Int = 1,
    ) = TaskDto.defeatUnits(unitId = unitId, count = count)

    fun talkToNpc(npcId: String = "farmer") = TaskDto.talkToNpc(npcId = npcId)

    fun reachTiles(vararg tiles: Pair<Int, Int>) = TaskDto.reachTiles(tiles = tiles.map { (row, column) -> PositionDto(row = row, column = column) })

    fun reward(
        gold: Int = 150,
        lootableItemIds: List<String> = emptyList(),
    ) = RewardDto(gold = gold, lootableItemIds = lootableItemIds)

    fun id() = "quest-${UUID.randomUUID()}"
}
