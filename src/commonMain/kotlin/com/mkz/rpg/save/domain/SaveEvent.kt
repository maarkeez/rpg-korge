package com.mkz.rpg.save.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface SaveEvent : DomainEvent {
    /** The game was saved into the slot, e.g.: from the pause screen. */
    data class GameSaved(
        val slot: Int,
    ) : SaveEvent

    /**
     * The game saved in the slot was restored: every subdomain is back as it was saved, without announcing it, so the
     * screen draws it all again.
     */
    data class GameRestored(
        val slot: Int,
    ) : SaveEvent

    /** The game saved in the slot was deleted, so the slot is empty. */
    data class SaveSlotDeleted(
        val slot: Int,
    ) : SaveEvent
}
