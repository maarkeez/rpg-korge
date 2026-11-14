package com.mkz.tessavar.unit.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.unit.domain.UnitEvent
import com.mkz.tessavar.unit.usecases.commands.InitialiseUnits

class OnRequestInitialiseUnits(
    initialiseUnits: InitialiseUnits,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<UnitEvent.RequestInitialiseUnits> {
            initialiseUnits()
        }
}
