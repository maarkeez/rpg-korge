package com.mkz.tessavar.lootableItem.adapters.events

import com.mkz.tessavar.lootableItem.domain.LootableItemEvent
import com.mkz.tessavar.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseLootableItems(
    initialiseLootableItems: InitialiseLootableItems,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootableItemEvent.RequestInitialiseLootableItems> {
            initialiseLootableItems()
        }
}
