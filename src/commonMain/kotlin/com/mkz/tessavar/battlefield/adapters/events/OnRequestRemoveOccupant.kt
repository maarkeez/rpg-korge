package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.tessavar.battlefield.usecases.commands.RemoveOccupant
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRemoveOccupant(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRemoveOccupant> { event ->
            removeOccupant(occupantId = event.occupantId)
        }
}
