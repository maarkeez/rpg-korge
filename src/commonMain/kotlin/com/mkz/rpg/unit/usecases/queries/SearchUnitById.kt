package com.mkz.rpg.unit.usecases.queries

import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitRepository

class SearchUnitById(
    private val unitRepository: UnitRepository,
) {
    operator fun invoke(id: String): Unit.Dto? = unitRepository.searchById(id)?.toDto()
}
