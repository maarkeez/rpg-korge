package com.mkz.rpg.unit.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.events.OnRequestInitialiseUnits
import com.mkz.rpg.unit.adapters.events.OnRequestUnitCreation
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitRepository
import com.mkz.rpg.unit.usecases.commands.InitialiseUnits
import com.mkz.rpg.unit.usecases.commands.RequestUnitCreation
import com.mkz.rpg.unit.usecases.queries.SearchMaximumAbilities
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import com.mkz.rpg.unit.usecases.services.UnitLoader

class UnitApi(
    eventBus: EventBus,
    private val unitLoader: UnitLoader,
) {
    // Storage
    private val unitRepository: UnitRepository = InMemoryUnitRepository()

    // Commands
    val requestUnitCreation = RequestUnitCreation(unitRepository, eventBus)
    val initialiseUnits = InitialiseUnits(unitLoader, unitRepository, eventBus)

    // Queries
    val searchUnitById: SearchUnitById = SearchUnitById(unitRepository)
    val searchMaximumAbilities: SearchMaximumAbilities = SearchMaximumAbilities()

    // Events
    private val onRequestUnitCreation = OnRequestUnitCreation(requestUnitCreation, eventBus)
    private val onRequestInitialiseUnits = OnRequestInitialiseUnits(initialiseUnits, eventBus)

    suspend fun init() {
        unitLoader.initResources()
    }
}
