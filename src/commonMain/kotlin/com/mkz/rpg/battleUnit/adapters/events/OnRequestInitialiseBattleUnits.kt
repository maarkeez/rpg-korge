package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseBattleUnits(
    initialiseBattleUnits: InitialiseBattleUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestInitialiseBattleUnits> {
            initialiseBattleUnits()
        }
}
