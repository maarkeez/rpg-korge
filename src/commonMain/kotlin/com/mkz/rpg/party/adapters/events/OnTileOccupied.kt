package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.party.usecases.commands.ResolvePartyStep
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnTileOccupied(
    resolvePartyStep: ResolvePartyStep,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TileOccupied> { event ->
            resolvePartyStep(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
