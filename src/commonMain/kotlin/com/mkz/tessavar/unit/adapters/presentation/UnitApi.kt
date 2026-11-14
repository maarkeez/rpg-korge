package com.mkz.tessavar.unit.adapters.presentation

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.unit.adapters.events.OnRequestInitialiseUnits
import com.mkz.tessavar.unit.adapters.events.OnRequestUnitCreation
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitRepository
import com.mkz.tessavar.unit.usecases.commands.InitialiseUnits
import com.mkz.tessavar.unit.usecases.commands.RequestUnitCreation
import com.mkz.tessavar.unit.usecases.queries.SearchMaximumAbilities
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import com.mkz.tessavar.unit.usecases.services.UnitLoader

class UnitApi(
    eventBus: EventBus,
    private val unitLoader: UnitLoader,
) {
    private val unitRepository: UnitRepository = InMemoryUnitRepository()

    val requestUnitCreation = RequestUnitCreation(unitRepository, eventBus)
    val initialiseUnits = InitialiseUnits(unitLoader, unitRepository, eventBus)

    val searchUnitById: SearchUnitById = SearchUnitById(unitRepository)
    val searchMaximumAbilities: SearchMaximumAbilities = SearchMaximumAbilities()

    private val onRequestUnitCreation = OnRequestUnitCreation(requestUnitCreation, eventBus)
    private val onRequestInitialiseUnits = OnRequestInitialiseUnits(initialiseUnits, eventBus)

    suspend fun init() {
        unitLoader.initResources()
    }
}
