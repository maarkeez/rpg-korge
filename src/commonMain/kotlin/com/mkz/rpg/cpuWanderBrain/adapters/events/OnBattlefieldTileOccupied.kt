package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattlefieldTileOccupied(
    resolveWanderingStep: ResolveWanderingStep,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { event ->
            resolveWanderingStep(battleUnitId = event.occupantId, row = event.row, column = event.column)
        }
}
