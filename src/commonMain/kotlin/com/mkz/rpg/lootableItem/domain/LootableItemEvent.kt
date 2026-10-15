package com.mkz.rpg.lootableItem.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootableItemEvent : DomainEvent {
    data class LootableItemCreated(
        val lootableItemId: String,
    ) : LootableItemEvent

    object RequestInitialiseLootableItems : LootableItemEvent
}
