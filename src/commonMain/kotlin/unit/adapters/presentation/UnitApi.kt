package unit.adapters.presentation

import shared.domain.EventBus
import unit.adapters.storage.InMemoryUnitRepository
import unit.domain.UnitRepository
import unit.usecases.commands.RequestUnitCreation
import unit.usecases.queries.SearchUnitById

class UnitApi(eventBus: EventBus) {

    // Storage
    private val unitRepository: UnitRepository = InMemoryUnitRepository()

    // Commands
    val requestUnitCreation = RequestUnitCreation(unitRepository, eventBus)

    // Queries
    val searchUnitById: SearchUnitById = SearchUnitById(unitRepository)
}
