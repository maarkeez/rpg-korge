package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.tessavar.lootDrop.usecases.commands.DropItems
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestDropItems(
    dropItems: DropItems,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestDropItems> { event ->
            dropItems(lootableItemIds = event.lootableItemIds, row = event.row, column = event.column)
        }
}
