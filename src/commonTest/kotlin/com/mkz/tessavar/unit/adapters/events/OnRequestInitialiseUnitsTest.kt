package com.mkz.tessavar.unit.adapters.events

import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitEvent
import com.mkz.tessavar.unit.domain.UnitMother.unit
import com.mkz.tessavar.unit.usecases.commands.InitialiseUnits
import com.mkz.tessavar.unit.usecases.services.FakeUnitLoader
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseUnitsTest {
    private val eventBus = InMemoryEventBus()
    private val unitRepository = InMemoryUnitRepository()
    private val knight = unit(id = "knight").toDto()
    private val onRequestInitialiseUnits = OnRequestInitialiseUnits(InitialiseUnits(FakeUnitLoader(units = listOf(knight)), unitRepository, eventBus), eventBus)

    @Test
    fun `should initialise the units when the request is dispatched`() {
        // Given
        eventBus.publish(UnitEvent.RequestInitialiseUnits)
        // When
        eventBus.dispatch()
        // Then
        unitRepository.searchById("knight")?.toDto() shouldBe knight
    }
}
