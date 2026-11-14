package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.party.usecases.commands.ResolvePartyStep
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnTileOccupied(
    resolvePartyStep: ResolvePartyStep,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TileOccupied> { event ->
            resolvePartyStep(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
