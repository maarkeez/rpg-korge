package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestSwapOccupants
import com.mkz.tessavar.battlefield.usecases.commands.SwapOccupants
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestSwapOccupants(
    swapOccupants: SwapOccupants,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestSwapOccupants> { event ->
            swapOccupants(occupantId = event.occupantId, otherOccupantId = event.otherOccupantId)
        }
}
