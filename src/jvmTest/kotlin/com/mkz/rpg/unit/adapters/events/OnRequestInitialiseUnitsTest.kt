package com.mkz.rpg.unit.adapters.events

import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitEvent
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.commands.InitialiseUnits
import com.mkz.rpg.unit.usecases.services.FakeUnitLoader
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
