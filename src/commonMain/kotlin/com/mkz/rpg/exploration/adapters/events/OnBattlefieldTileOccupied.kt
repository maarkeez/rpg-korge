package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.exploration.usecases.commands.ResolvePartyStep
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattlefieldTileOccupied(
    resolvePartyStep: ResolvePartyStep,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { event ->
            resolvePartyStep(battleUnitId = event.battlefieldUnitId, row = event.row, column = event.column)
        }
}
