package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestMoveOccupant
import com.mkz.rpg.battlefield.usecases.commands.FindLoot
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestMoveOccupant(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    findLoot: FindLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestMoveOccupant> { event ->
            updateBattlefieldOccupancy(
                row = event.toRow,
                column = event.toColumn,
                occupantId = event.occupantId,
            )
            // A teleport's path is only its destination.
            findLoot(occupantId = event.occupantId, path = event.path)
        }
}
