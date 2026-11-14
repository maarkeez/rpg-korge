package com.mkz.tessavar.unit.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitRepository

class RequestUnitCreation(
    private val unitRepository: UnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(unitDto: Unit.Dto) {
        val (events, unit) = Unit.create(unitDto).pullEvents()
        unitRepository.create(unit)
        eventBus.publish(events)
    }
}
