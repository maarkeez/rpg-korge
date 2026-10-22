package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestSwapOccupants
import com.mkz.rpg.battlefield.usecases.commands.SwapOccupants
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestSwapOccupants(
    swapOccupants: SwapOccupants,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestSwapOccupants> { event ->
            swapOccupants(occupantId = event.occupantId, otherOccupantId = event.otherOccupantId)
        }
}
