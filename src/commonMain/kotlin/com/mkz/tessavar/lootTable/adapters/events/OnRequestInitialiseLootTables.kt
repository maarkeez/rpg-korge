package com.mkz.tessavar.lootTable.adapters.events

import com.mkz.tessavar.lootTable.domain.LootTableEvent
import com.mkz.tessavar.lootTable.usecases.commands.InitialiseLootTables
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseLootTables(
    initialiseLootTables: InitialiseLootTables,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootTableEvent.RequestInitialiseLootTables> {
            initialiseLootTables()
        }
}
