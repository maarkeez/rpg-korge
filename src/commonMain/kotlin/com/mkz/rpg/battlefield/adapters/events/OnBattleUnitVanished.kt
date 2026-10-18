package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitVanished(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitVanished> { event ->
            removeOccupant(battleUnitId = event.battleUnitId)
        }
}
