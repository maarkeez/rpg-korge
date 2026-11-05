package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestMoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.OccupyTile
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
