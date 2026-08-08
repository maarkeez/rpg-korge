package unit.usecases.commands

import shared.domain.EventBus
import unit.domain.Unit
import unit.domain.UnitRepository

class RequestUnitCreation(
    private val unitRepository: UnitRepository,
    private val eventBus: EventBus
) {

    operator fun invoke(unitDto: Unit.Dto) {
        val (events, unit) = Unit.create(unitDto).pullEvents()
        unitRepository.create(unit)
        eventBus.publish(events)
    }

}
