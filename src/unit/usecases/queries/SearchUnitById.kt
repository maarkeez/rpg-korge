package unit.usecases.queries

import unit.domain.UnitRepository

class SearchUnitById(
    private val unitRepository: UnitRepository,
) {
    operator fun invoke(id: String) = unitRepository.searchById(id)?.toDto()
}
