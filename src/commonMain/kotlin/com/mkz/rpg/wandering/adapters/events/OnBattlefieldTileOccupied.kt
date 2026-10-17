package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.ResolveWanderingStep

class OnBattlefieldTileOccupied(
    resolveWanderingStep: ResolveWanderingStep,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { event ->
            resolveWanderingStep(battleUnitId = event.battlefieldUnitId, row = event.row, column = event.column)
        }
}
