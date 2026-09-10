package com.mkz.rpg.unit.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitRepository
import com.mkz.rpg.unit.usecases.commands.RequestUnitCreation
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

class UnitApi(
    eventBus: EventBus,
) {
    // Storage
    private val unitRepository: UnitRepository = InMemoryUnitRepository()

    // Commands
    val requestUnitCreation = RequestUnitCreation(unitRepository, eventBus)

    // Queries
    val searchUnitById: SearchUnitById = SearchUnitById(unitRepository)
}
