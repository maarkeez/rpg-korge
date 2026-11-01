package com.mkz.rpg.quest.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface QuestEvent : DomainEvent {
    object RequestInitialiseQuests : QuestEvent

    object RequestStartMainQuest : QuestEvent

    data class QuestStarted(
        val questId: String,
    ) : QuestEvent
}
