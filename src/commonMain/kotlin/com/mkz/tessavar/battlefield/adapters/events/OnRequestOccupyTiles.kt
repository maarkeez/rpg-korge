package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestOccupyTiles
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTiles
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

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
