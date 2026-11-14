package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestMoveOccupant
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTile
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestMoveOccupant(
    occupyTile: OccupyTile,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestMoveOccupant> { event ->
            occupyTile(
                row = event.toRow,
                column = event.toColumn,
                occupantId = event.occupantId,
            )
        }
}
