package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.rpg.battlefield.usecases.commands.OccupyTiles
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestOccupyTiles(
    occupyTiles: OccupyTiles,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestOccupyTiles> { event ->
            occupyTiles(
                row = event.row,
                column = event.column,
                rows = event.rows,
                columns = event.columns,
                occupantId = event.occupantId,
            )
        }
}
