package com.mkz.tessavar.unit.usecases.queries

import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitRepository

class SearchUnitById(
    private val unitRepository: UnitRepository,
) {
    operator fun invoke(id: String): Unit.Dto? = unitRepository.searchById(id)?.toDto()
}
