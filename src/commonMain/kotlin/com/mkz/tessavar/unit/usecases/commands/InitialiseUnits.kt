package com.mkz.tessavar.unit.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitEvent
import com.mkz.tessavar.unit.domain.UnitRepository
import com.mkz.tessavar.unit.usecases.services.UnitLoader

class InitialiseUnits(
    private val unitLoader: UnitLoader,
    private val unitRepository: UnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<UnitEvent>()
        unitLoader.loadUnits().forEach { unitDto ->
            if (unitRepository.searchById(unitDto.id) != null) return@forEach
            val (unitEvents, unit) = Unit.create(unitDto).pullEvents()
            unitRepository.create(unit)
            events += unitEvents
        }
        eventBus.publish(events)
    }
}
