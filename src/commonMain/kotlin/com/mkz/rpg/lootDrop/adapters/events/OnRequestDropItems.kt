package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.rpg.lootDrop.usecases.commands.DropItems
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestDropItems(
    dropItems: DropItems,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestDropItems> { event ->
            dropItems(lootableItemIds = event.lootableItemIds, row = event.row, column = event.column)
        }
}
