package com.mkz.rpg.quest.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface QuestEvent : DomainEvent {
    object RequestInitialiseQuests : QuestEvent

    object RequestStartMainQuest : QuestEvent

    data class QuestStarted(
        val questId: String,
    ) : QuestEvent

    /** A battle unit asked for by the current task was defeated: it counts [progress] of [count]. */
    data class TaskProgressed(
        val questId: String,
        val taskIndex: Int,
        val progress: Int,
        val count: Int,
    ) : QuestEvent

    data class TaskCompleted(
        val questId: String,
        val taskIndex: Int,
    ) : QuestEvent

    /** The last task was completed: the reward is requested for whoever leads the party. */
    data class QuestCompleted(
        val questId: String,
    ) : QuestEvent
}
