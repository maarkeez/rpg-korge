package com.mkz.rpg.quest.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface QuestEvent : DomainEvent {
    object RequestCreateQuests : QuestEvent

    data class QuestStarted(
        val questId: String,
    ) : QuestEvent

    /** A battle unit asked for by the current defeat task was defeated: it counts [defeats] of [requiredDefeats]. */
    data class DefeatCounted(
        val questId: String,
        val taskIndex: Int,
        val defeats: Int,
        val requiredDefeats: Int,
    ) : QuestEvent

    data class TaskCompleted(
        val questId: String,
        val taskIndex: Int,
    ) : QuestEvent

    /** The side quest went back to not started, with its progress reset. */
    data class QuestAbandoned(
        val questId: String,
    ) : QuestEvent

    /** The quest is the pinned quest: the main quest, when no side quest is pinned, once it has started. */
    data class QuestPinned(
        val questId: String,
    ) : QuestEvent

    /** The last task was completed: the reward is requested for whoever leads the party. */
    data class QuestCompleted(
        val questId: String,
    ) : QuestEvent
}
