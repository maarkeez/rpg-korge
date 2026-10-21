package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestPickUpDroppedItem
import com.mkz.rpg.battlefield.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestPickUpDroppedItem(
    pickUpDroppedItem: PickUpDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestPickUpDroppedItem> { event ->
            pickUpDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
