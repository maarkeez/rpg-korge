package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto

/** Maps a quest to what the quest views draw, translated. The tasks after the current one are left out, so they can not be revealed. */
internal object QuestMapper {
    /** The main quest is pinned when no other quest is. */
    fun questInfos(
        quests: List<Quest.Dto>,
        texts: Texts,
    ): List<QuestInfo> {
        val isSideQuestPinned = quests.any { quest -> quest.progress.isPinned }
        return quests.map { quest -> questInfo(quest, texts, isPinned = quest.progress.isPinned || (quest.type == QuestTypeDto.MAIN && !isSideQuestPinned)) }
    }

    private fun questInfo(
        quest: Quest.Dto,
        texts: Texts,
        isPinned: Boolean,
    ): QuestInfo {
        val isCompleted = quest.progress.status == StatusDto.COMPLETED
        val currentTaskIndex = quest.progress.currentTaskIndex
        return QuestInfo(
            id = quest.id,
            name = texts.questName(quest.id),
            description = texts.questDescription(quest.id),
            tasks =
                quest.tasks.take(currentTaskIndex + 1).mapIndexed { taskIndex, task ->
                    val count = countOf(task)
                    val isDone = isCompleted || taskIndex < currentTaskIndex
                    QuestInfo.Task(
                        kind = kindOf(task),
                        text = texts.questTask(quest.id, taskIndex),
                        progress = if (isDone) count else quest.progress.currentTaskProgress,
                        count = count,
                        portraitPath = portraitPathOf(task),
                        isDone = isDone,
                    )
                },
            gold = quest.reward.gold,
            items = quest.reward.lootableItemIds.map { lootableItemId -> QuestInfo.RewardItem(id = lootableItemId, name = texts.lootableItemName(lootableItemId)) },
            isCompleted = isCompleted,
            isMain = quest.type == QuestTypeDto.MAIN,
            isRepeatable = quest.repeatable,
            isPinned = isPinned,
        )
    }

    private fun kindOf(task: TaskDto): QuestInfo.Kind =
        when (task.type) {
            TaskDto.TypeDto.DEFEAT_UNITS -> QuestInfo.Kind.DEFEAT
            TaskDto.TypeDto.TALK_TO_NPC -> QuestInfo.Kind.TALK
            TaskDto.TypeDto.REACH_TILES -> QuestInfo.Kind.REACH
        }

    // A talk or a reach has nothing to count.
    private fun countOf(task: TaskDto): Int = task.defeatUnits?.count ?: 1

    private fun portraitPathOf(task: TaskDto): String? =
        when (task.type) {
            TaskDto.TypeDto.DEFEAT_UNITS -> task.defeatUnits?.let { defeatUnits -> "unit/${defeatUnits.unitId}_portrait.png" }
            TaskDto.TypeDto.TALK_TO_NPC -> task.talkToNpc?.let { talkToNpc -> "npc/${talkToNpc.npcId}_portrait.png" }
            TaskDto.TypeDto.REACH_TILES -> null
        }
}
