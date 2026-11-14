package com.mkz.tessavar.unit.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.unit.domain.UnitEvent
import com.mkz.tessavar.unit.usecases.commands.RequestUnitCreation

class OnRequestUnitCreation(
    private val requestUnitCreation: RequestUnitCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<UnitEvent.RequestUnitCreation> { event ->
            requestUnitCreation(event.unitDto)
        }
}
