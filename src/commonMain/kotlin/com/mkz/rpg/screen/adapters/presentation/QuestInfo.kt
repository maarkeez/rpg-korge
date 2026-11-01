package com.mkz.rpg.screen.adapters.presentation

/**
 * The plain data the quest views display, already translated. Tasks: the ones done and the current one, as those still
 * to come are never revealed.
 */
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
) {
    enum class Kind { DEFEAT, TALK, REACH }

    /** Count: only above 1 for a task to defeat several. Portrait path: who the task is about, e.g.: `unit/rat_portrait.png`, or null for a place. */
    data class Task(
        val kind: Kind,
        val text: String,
        val progress: Int = 0,
        val count: Int = 1,
        val portraitPath: String? = null,
        val isDone: Boolean = false,
    )

    data class RewardItem(
        val id: String,
        val name: String,
    )

    /** The task under way, or the last one once the quest is completed. */
    val currentTask: Task get() = tasks.lastOrNull { task -> !task.isDone } ?: tasks.last()

    val doneTasks: List<Task> get() = tasks.filter { task -> task.isDone }
}
