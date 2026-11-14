package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTile
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

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
