package unit.usecases.queries

import unit.domain.Unit
import unit.domain.UnitRepository

class SearchUnitById(
    private val unitRepository: UnitRepository,
) {
    operator fun invoke(id: String): Unit.Dto? = unitRepository.searchById(id)?.toDto()
}
