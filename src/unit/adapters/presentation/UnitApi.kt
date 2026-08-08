package unit.adapters.presentation

import unit.adapters.storage.InMemoryUnitRepository
import unit.domain.UnitRepository
import unit.usecases.queries.SearchUnitById

class UnitApi {

    // Storage
    private val unitRepository: UnitRepository = InMemoryUnitRepository()

    // Queries
    val searchUnitById: SearchUnitById = SearchUnitById(unitRepository)
}
