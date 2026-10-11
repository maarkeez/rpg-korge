package com.mkz.rpg.unit.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.domain.UnitEvent
import com.mkz.rpg.unit.usecases.commands.InitialiseUnits

class OnRequestInitialiseUnits(
    initialiseUnits: InitialiseUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<UnitEvent.RequestInitialiseUnits> {
            initialiseUnits()
        }
}
