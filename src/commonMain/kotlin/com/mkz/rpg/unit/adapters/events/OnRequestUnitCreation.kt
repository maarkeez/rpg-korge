package com.mkz.rpg.unit.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.domain.UnitEvent
import com.mkz.rpg.unit.usecases.commands.RequestUnitCreation

class OnRequestUnitCreation(
    private val requestUnitCreation: RequestUnitCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<UnitEvent.RequestUnitCreation> { event ->
            requestUnitCreation(event.unitDto)
        }
}
