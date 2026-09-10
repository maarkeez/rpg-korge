package com.mkz.rpg.unit.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitRepository

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
