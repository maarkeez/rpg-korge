package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.tessavar.screen.adapters.presentation.QuestInfo.Task

object QuestInfoMother {
    fun questInfo(
        tasks: List<Task> = listOf(task()),
        gold: Int = 150,
        items: List<QuestInfo.RewardItem> = emptyList(),
        name: String = "The Goat Wizard's Lair",
        description: String = "A goat swallowed a wizard's magic and now rules the lair down south.",
        isCompleted: Boolean = false,
        id: String = "defeat-the-goat-wizard",
        isMain: Boolean = true,
        isRepeatable: Boolean = false,
        isPinned: Boolean = false,
    ) = QuestInfo(
        id = id,
        name = name,
        description = description,
        tasks = tasks,
        gold = gold,
        items = items,
        isCompleted = isCompleted,
        isMain = isMain,
        isRepeatable = isRepeatable,
        isPinned = isPinned,
    )

    fun task(
        text: String = "Defeat the Goat Wizard",
        kind: Kind = Kind.DEFEAT,
        defeatsCounted: Int = 0,
        requiredDefeats: Int = 1,
        portraitPath: String? = null,
        isDone: Boolean = false,
    ) = Task(kind = kind, text = text, defeatsCounted = defeatsCounted, requiredDefeats = requiredDefeats, portraitPath = portraitPath, isDone = isDone)

    fun completedQuestInfo() = questInfo(tasks = listOf(task(defeatsCounted = 1, isDone = true)), isCompleted = true)
}
