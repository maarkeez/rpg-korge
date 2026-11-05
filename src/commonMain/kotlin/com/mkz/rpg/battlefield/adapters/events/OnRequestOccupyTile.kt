package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.rpg.battlefield.usecases.commands.OccupyTile
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestOccupyTile(
    occupyTile: OccupyTile,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestOccupyTile> { event ->
            occupyTile(
                row = event.row,
                column = event.column,
                occupantId = event.occupantId,
            )
        }
}
