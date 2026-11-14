package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnTileOccupied(
    resolveWanderingStep: ResolveWanderingStep,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattlefieldEvent.TileOccupied> { event ->
            resolveWanderingStep(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
