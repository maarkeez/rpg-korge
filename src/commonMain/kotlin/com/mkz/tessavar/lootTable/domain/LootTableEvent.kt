package com.mkz.tessavar.lootTable.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface LootTableEvent : DomainEvent {
    data class LootTableCreated(
        val lootTableId: String,
    ) : LootTableEvent

    object RequestInitialiseLootTables : LootTableEvent
}
