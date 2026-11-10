package com.mkz.rpg.save.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface SaveEvent : DomainEvent {
    data class GameSaved(
        val slot: Int,
    ) : SaveEvent

    data class GameLoaded(
        val slot: Int,
    ) : SaveEvent

    data class SaveDeleted(
        val slot: Int,
    ) : SaveEvent
}
