package com.mkz.tessavar.screen.adapters.presentation

data class QuestInfo(
    val id: String,
    val name: String,
    val description: String,
    val tasks: List<Task>,
    val gold: Int,
    val items: List<RewardItem> = emptyList(),
    val isCompleted: Boolean = false,
    val isMain: Boolean = false,
    val isRepeatable: Boolean = false,
    val isPinned: Boolean = false,
) {
    enum class Kind { DEFEAT, TALK, VISIT }

    data class Task(
        val kind: Kind,
        val text: String,
        val defeatsCounted: Int = 0,
        val requiredDefeats: Int = 1,
        val portraitPath: String? = null,
        val isDone: Boolean = false,
    )

    data class RewardItem(
        val id: String,
        val name: String,
    )

    val currentTask: Task get() = tasks.lastOrNull { task -> !task.isDone } ?: tasks.last()

    val doneTasks: List<Task> get() = tasks.filter { task -> task.isDone }
}
