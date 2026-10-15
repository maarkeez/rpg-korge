package com.mkz.rpg.lootableItem.adapters.events

import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseLootableItems(
    initialiseLootableItems: InitialiseLootableItems,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootableItemEvent.RequestInitialiseLootableItems> {
            initialiseLootableItems()
        }
}
