package com.mkz.rpg.quest.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface QuestEvent : DomainEvent {
    object RequestCreateQuests : QuestEvent

    data class QuestStarted(
        val questId: String,
    ) : QuestEvent

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

    data class QuestAbandoned(
        val questId: String,
    ) : QuestEvent

    data class QuestPinned(
        val questId: String,
    ) : QuestEvent

    data class QuestCompleted(
        val questId: String,
    ) : QuestEvent
}
