package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestLineDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto

internal object QuestMapper {
    fun questInfos(
        quests: List<Quest.Dto>,
        messages: Messages,
    ): List<QuestInfo> {
        val isSideQuestPinned = quests.any { quest -> quest.progress.isPinned }
        return quests.map { quest ->
            questInfo(
                quest,
                messages,
                isPinned =
                    quest.progress.isPinned || (quest.type == QuestTypeDto.MAIN && quest.progress.status != StatusDto.NOT_STARTED && !isSideQuestPinned),
            )
        }
    }

    private fun questInfo(
        quest: Quest.Dto,
        messages: Messages,
        isPinned: Boolean,
    ): QuestInfo {
        val isCompleted = quest.progress.status == StatusDto.COMPLETED
        val currentTaskIndex = quest.progress.currentTaskIndex
        return QuestInfo(
            id = quest.id,
            name = messages.questName(quest.id),
            description = messages.questDescription(quest.id),
            tasks =
                quest.tasks.take(currentTaskIndex + 1).mapIndexed { taskIndex, task ->
                    val requiredDefeats = requiredDefeatsOf(task)
                    val isDone = isCompleted || taskIndex < currentTaskIndex
                    QuestInfo.Task(
                        kind = kindOf(task),
                        text = messages.questTask(quest.id, taskIndex),
                        defeatsCounted = if (isDone) requiredDefeats else quest.progress.currentTaskDefeats,
                        requiredDefeats = requiredDefeats,
                        portraitPath = portraitPathOf(task),
                        isDone = isDone,
                    )
                },
            gold = quest.reward.gold,
            items = quest.reward.lootableItemIds.map { lootableItemId -> QuestInfo.RewardItem(id = lootableItemId, name = messages.lootableItemName(lootableItemId)) },
            isCompleted = isCompleted,
            isMain = quest.type == QuestTypeDto.MAIN,
            isRepeatable = quest.repeatable,
            isPinned = isPinned,
        )
    }

    fun signOf(sign: QuestMarkerDto.SignDto): QuestMarkerView.Sign =
        when (sign) {
            QuestMarkerDto.SignDto.QUEST_TO_START -> QuestMarkerView.Sign.QUEST_TO_START
            QuestMarkerDto.SignDto.TALK_TASK -> QuestMarkerView.Sign.TALK_TASK
        }

    fun lineOf(
        line: QuestLineDto,
        messages: Messages,
    ): String =
        messages.questLine(
            line.questId,
            when (line.kind) {
                QuestLineDto.KindDto.START -> "start"
                QuestLineDto.KindDto.REMINDER -> "reminder"
                QuestLineDto.KindDto.TALK_TASK -> "task.${line.taskIndex}"
                QuestLineDto.KindDto.COMPLETED -> "completed"
                QuestLineDto.KindDto.START_AGAIN -> "startAgain"
            },
        )

    private fun kindOf(task: TaskDto): QuestInfo.Kind =
        when (task.type) {
            TaskDto.TypeDto.DEFEAT -> QuestInfo.Kind.DEFEAT
            TaskDto.TypeDto.TALK -> QuestInfo.Kind.TALK
            TaskDto.TypeDto.VISIT -> QuestInfo.Kind.VISIT
        }

    private fun requiredDefeatsOf(task: TaskDto): Int = task.defeat?.requiredDefeats ?: 1

    private fun portraitPathOf(task: TaskDto): String? =
        when (task.type) {
            TaskDto.TypeDto.DEFEAT -> task.defeat?.let { defeat -> "unit/${defeat.unitId}_portrait.png" }
            TaskDto.TypeDto.TALK -> task.talk?.let { talk -> "npc/${talk.npcId}_portrait.png" }
            TaskDto.TypeDto.VISIT -> null
        }
}
