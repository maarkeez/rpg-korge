package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Task

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
        progress: Int = 0,
        count: Int = 1,
        portraitPath: String? = null,
        isDone: Boolean = false,
    ) = Task(kind = kind, text = text, progress = progress, count = count, portraitPath = portraitPath, isDone = isDone)

    fun completedQuestInfo() = questInfo(tasks = listOf(task(progress = 1, isDone = true)), isCompleted = true)
}
