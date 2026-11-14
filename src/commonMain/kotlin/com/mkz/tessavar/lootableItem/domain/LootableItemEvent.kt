package com.mkz.tessavar.lootableItem.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface LootableItemEvent : DomainEvent {
    data class LootableItemCreated(
        val lootableItemId: String,
    ) : LootableItemEvent

    object RequestInitialiseLootableItems : LootableItemEvent
}
